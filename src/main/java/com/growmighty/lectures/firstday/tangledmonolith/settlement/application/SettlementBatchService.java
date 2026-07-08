package com.growmighty.lectures.firstday.tangledmonolith.settlement.application;

import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto.SettleReport;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.support.HeapMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Service;

// 두 번째 실험. 정산 배치 Job 실행기
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementBatchService {
    private final JobOperator jobOperator;
    private final Job settlementJob;

    public SettleReport run() {
        try (HeapMonitor monitor = HeapMonitor.start("batch", 500)) {
            long startedAt = System.currentTimeMillis();

            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            log.warn("[BATCH] 정산 Job 실행");
            JobExecution execution = jobOperator.start(settlementJob, params);

            long read = 0;
            long written = 0;
            for (StepExecution step : execution.getStepExecutions()) {
                read += step.getReadCount();
                written += step.getWriteCount();
            }
            long elapsed = System.currentTimeMillis() - startedAt;

            SettleReport report = new SettleReport(
                read, written, elapsed, monitor.peakUsedMb(), monitor.maxHeapMb());
            log.warn("[BATCH] 완료. status={}, 리포트={}", execution.getStatus(), report);
            return report;
        } catch (Exception e) {
            throw new IllegalStateException("정산 배치 실행 실패: " + e.getMessage(), e);
        }
    }
}
