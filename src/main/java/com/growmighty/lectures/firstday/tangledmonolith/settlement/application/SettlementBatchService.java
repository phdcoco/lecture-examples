package com.growmighty.lectures.firstday.tangledmonolith.settlement.application;

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto.SettleReport;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.batch.SettlementFaultBox;
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

    public SettleReport run() {
        faultBox.disarm(); // 깨끗한 정상 상태에서 시작.
        JobParameters params = newRunParams();
        log.warn("[BATCH] 정산 Job 실행 (새 인스턴스)");
        return launch(params, "batch");
    }

    // failRatio는 전체 일의 failRatio 비율만큼 성공 후 실패
    public SettleReport runFailing(double failRatio) {
        long remaining = orderRepository.count() - settlementRepository.count();
        long failAfter = Math.max(1, Math.round(remaining * failRatio));
        faultBox.arm(failAfter); // 실패 지점 전달

        JobParameters params = newRunParams();
        log.warn("[BATCH] 장애 주입 실행 예정 -> 남은 {}건 중 {}건 처리 후 실패 예정", remaining, failAfter);

        return launch(params, "batch-fail");
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
        return launch(params, "batch-restart");
    }

    private SettleReport launch(JobParameters params, String label) {
        try (HeapMonitor monitor = HeapMonitor.start(label, 500)) {
            long startedAt = System.currentTimeMillis();
            JobExecution execution = jobOperator.start(settlementJob, params);

            long read = 0, written = 0, skipped = 0;
            for (StepExecution step : execution.getStepExecutions()) {
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
