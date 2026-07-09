package com.growmighty.lectures.firstday.settlement.batch;

import com.growmighty.lectures.firstday.order.domain.Order;
import com.growmighty.lectures.firstday.order.domain.OrderStatus;
import com.growmighty.lectures.firstday.settlement.domain.Settlement;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SettlementParallelJobFactory {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderToSettlementProcessor settlementProcessor;
    private final JpaItemWriter<Settlement> settlementWriter;
    private final Step settlementWorkerStep;
    private final EntityManagerFactory entityManagerFactory;
    private final JdbcTemplate jdbcTemplate;

    @Value("${settlement.batch.chunk-size:1000}")
    private int chunkSize;

    // Chunk들을 threads 개수만큼 동시에 각자 나눠 통째로 처리한다.
    @SuppressWarnings("removal")
    public Job multiThreadedJob(int threads) {
        // 멀티스레드를 실행할 Executor를 생성한다. 실행되는 스레드 이름은 mt-worker-1 ...
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("mt-worker-");
        executor.setConcurrencyLimit(threads); // 최대 threads 개수만큼의 thread를 생성한다.

        Step step = new StepBuilder("settlementMultiThreadedStep", jobRepository)
            .<Order, Settlement>chunk(chunkSize, transactionManager) // transactionManager 과정을 하나의 chunk로 만든다.
            .reader(multiThreadedReader()) // Order를 읽어온다.
            .processor(settlementProcessor) // Order를 로직을 통과한 Settlement로 만든다.
            .writer(settlementWriter) // Settlement를 DB에 저장한다.
            .taskExecutor(executor) // Chunk를 여러 스레드에서 처리하라.
            .build(); // Step 객체 생성 완료.

        return new JobBuilder("settlementMultiThreadedJob", jobRepository)
            .start(step)
            .build();
    }

    // 진짜 정답은 이것이다.
    public Job partitionedJob(int gridSize) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("part-worker-");
        executor.setConcurrencyLimit(gridSize);

        Step masterStep = new StepBuilder("settlementPartitionedStep", jobRepository)
            .partitioner("settlementWorkerStep", new OrderRangePartitioner(jdbcTemplate))
            .step(settlementWorkerStep)
            .gridSize(gridSize)
            .taskExecutor(executor)
            .build();

        return new JobBuilder("settlementPArtitionedJob", jobRepository)
            .start(masterStep)
            .build();
    }

    // PAID 상태인 Order를 페이지 단위로 읽어오는 Reader 생성.
    private JpaPagingItemReader<Order> multiThreadedReader() {
        return new JpaPagingItemReaderBuilder<Order>()
            .name("settlementMultiThreadedReader")
            .entityManagerFactory(entityManagerFactory) // JPA를 활용한다.
            // JPQL를 활용하여 SQL 쿼리를 직접 작성한다. 정렬은 필수다.
            .queryString("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.id ASC")
            .parameterValues(Map.of("status", OrderStatus.PAID)) // SQL 구문의 status에 넣는다.
            .pageSize(chunkSize) // 페이지 사이즈는 청크 사이즈로 정의한다.
            .saveState(false) // 멀티스레드이므로 상태 저장을 끈다. 재시작 기능을 포기하는 대신 멀티스레드 안전성을 얻는다.
            .build();
    }
}
