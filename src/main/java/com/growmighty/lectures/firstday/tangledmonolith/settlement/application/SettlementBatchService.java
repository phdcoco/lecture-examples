package com.growmighty.lectures.firstday.tangledmonolith.settlement.application;

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto.SettleReport;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.batch.SettlementFaultBox;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.batch.SettlementParallelJobFactory;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.SettlementRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.support.HeapMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * [Step2] 정산 배치 Job 실행기
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementBatchService {

    private final JobOperator jobOperator;
    private final Job settlementJob;
    private final SettlementFaultBox faultBox;
    private final OrderRepository orderRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementParallelJobFactory parallelJobFactory;

    // 멀티스레드 테스트
    // 기본 스레드 수
    @Value("${settlement.batch.thread-count:8}")
    private int defaultThreadCount;

    // 파티셔닝의 기본 gridSize
    @Value("${settlement.batch.grid-size:8}")
    private int defaultGridSize;

    public SettleReport run() {
        faultBox.disarm(); // 깨끗한 정상 상태에서 시작.
        JobParameters params = newRunParams();
        log.warn("[BATCH] 정산 Job 실행 (새 인스턴스)");
        return launch(settlementJob, params, "batch");
    }

    // 멀티 스레드 실행, 그러나 스레드만 늘리는 순진한 가속이다. 과연 이 방법이 먹힐까?
    // 읽기 깔때기 : 물통을 아무리 많이 갖다 놔도 수도꼭지에서 나오는 물의 양과 속도는 일정하다.
    // 처리량 천장 : 스레드가 일정량 이상 늘어나면 1초에 처리하는 정보의 양이 더 이상 늘어나지 않는다. 그 값.
    public SettleReport runMultiThreaded(Integer threads) {
        faultBox.disarm();
        int t = (threads != null) ? threads : defaultThreadCount;
        // 효과가 있다면 처리량 천장이 드라마틱하게 늘어날 것이다. 과연?
        log.warn("[BATCH] Multi-threaded Step 실행 - threads = {} (읽기 깔때기, 처리량 천장 측정)", t);
        return launch(parallelJobFactory.multiThreadedJob(t), newRunParams(), "batch-mt");
    }

    // 멀티 스레드의 재시작 위치 상실 시 재시작 진행을 지켜보자.
    // 멀티스레드에서 Reader는 saveState가 false라 내가 어디까지 읽었는지를 알지 못한다.
    // 따라서 실패 후 재시작하면 무조건 처음부터 다시 읽는다. -> 멱등성이 이중정산은 막지만, "이어서 재개"의 장점은 사라진다.
    public SettleReport runMultiThreadedRestartable(long runId, Double failRatio) {
        if (failRatio == null) {
            faultBox.disarm();
        } else {
            long remaining = orderRepository.count() - settlementRepository.count();
            faultBox.arm(Math.max(1, Math.round(remaining * failRatio)));
        }
        JobParameters params = new JobParametersBuilder()
            .addLong("runId", runId)
            .toJobParameters();
        log.warn("[BATCH] Multi-Threaded 재시작 시도 -> runId = {}, 장애 = {} (saveState가 false이므로 처음부터 다시 읽는다.)",
            runId, faultBox.armed());
        return launch(parallelJobFactory.multiThreadedJob(defaultThreadCount), params, "batch-mt-restart");
    }

    // id 범위를 gridSize개로 나눠 워커마다 전용 Reader로 병렬 처리.
    public SettleReport runPartitioned(Integer gridSize) {
        faultBox.disarm();
        int g = (gridSize != null) ? gridSize : defaultGridSize; // 1
        log.warn("[BATCH] Partitioning 실행 -> gridSize = {} (구조적 해법 : 속도와 재시작 둘 다 용이)", g);
        return launch(parallelJobFactory.partitionedJob(g), newRunParams(), "batch-part");
    }

    // failRatio는 전체 일의 failRatio 비율만큼 성공 후 실패
    public SettleReport runFailing(double failRatio) {
        long remaining = orderRepository.count() - settlementRepository.count();
        long failAfter = Math.max(1, Math.round(remaining * failRatio));
        faultBox.arm(failAfter); // 실패 지점 전달

        JobParameters params = newRunParams();
        log.warn("[BATCH] 장애 주입 실행 예정 -> 남은 {}건 중 {}건 처리 후 실패 예정", remaining, failAfter);

        return launch(settlementJob, params, "batch-fail");
    }

    public SettleReport runRestartable(long runId, Double failRatio) {
        if (failRatio == null) {
            faultBox.disarm();
        }
        else {
            long remaining = orderRepository.count() - settlementRepository.count();
            faultBox.arm(Math.max(1, Math.round(remaining * failRatio)));
        }

        // runId가 같다면 같은 JobInstance니까 재시작 대상이다.
        JobParameters params = new JobParametersBuilder()
            .addLong("runId", runId)
            .toJobParameters();

        log.warn("[BATCH] 재시작 가능 실행: runId = {}, 장애 = {}", runId, faultBox.armed());
        return launch(settlementJob, params, "batch-restart");
    }

    private SettleReport launch(Job job, JobParameters params, String label) {
        try (HeapMonitor monitor = HeapMonitor.start(label, 500)) {
            long startedAt = System.currentTimeMillis();
            JobExecution execution = jobOperator.start(job, params);

            long read = 0, written = 0, skipped = 0;
            for (StepExecution step : execution.getStepExecutions()) {
                // 파티셔닝의 워커 StepExecution은 마스터 Step에 합산되어있으니 뺀다.
                if (step.getStepName().contains(":")) {
                    continue;
                }

                read += step.getReadCount();
                written += step.getWriteCount();
                skipped += step.getFilterCount();
            }
            long elapsed = System.currentTimeMillis() - startedAt;

            SettleReport report = new SettleReport(read, written, skipped, elapsed,
                monitor.peakUsedMb(), monitor.maxHeapMb(), execution.getStatus().toString());
            log.warn("[BATCH] 정상 종료. status={}, 리포트={}", execution.getStatus(), report);
            return report;
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new IllegalStateException(
                "이미 완료된 정산 인스턴스입니다! (같은 파라미터, 멱등성 실현 성공) 새 runId 를 쓰거나 DELETE /settlements 후 다시 시작하세요.", e);
        } catch (Exception e) {
            throw new IllegalStateException("정산 배치 실행 실패: " + e.getMessage(), e);
        }
    }

    private JobParameters newRunParams() {
        return new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
    }
}
