package com.growmighty.lectures.firstday.tangledmonolith.settlement.batch;

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.Order;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderStatus;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.Settlement;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class SettlementParallelJobConfig {

    @Value("${settlement.batch.chunk-size:1000}")
    private int chunkSize;

    // 멀티스레드에서는 Reader가 하나라서 Reader 병목이 있었다.
    // Partitioning은 아예 여러 리더를 만들어서 읽기 자체를 병렬화한다.
    // 워커 Reader : 자기 파티션의 id 범위에 해당하는 PAID 주문만 페이지로 읽는다.
    @Bean
    @StepScope // Partition마다 새 Reader를 Bean을 이용해 생성한다.
    public JpaPagingItemReader<Order> settlementWorkerReader(
        EntityManagerFactory emf,
        // 여기서 Partitioner가 넣어준 minId, maxId를 꺼낸다.
        @Value("#{stepExecutionContext['minId']}") Long minId,
        @Value("#{stepExecutionContext['maxId']}") Long maxId) {
        return new JpaPagingItemReaderBuilder<Order>()
            .name("settlementWorkerReader")
            .entityManagerFactory(emf)
            // JPQL 이용, 정해진 id만큼의 정보를 읽는다.
            .queryString("SELECT o FROM Order o "
            + "WHERE o.status = :status AND o.id BETWEEN :minId AND :maxId "
            + "ORDER BY o.id ASC")
            .parameterValues(Map.of(
                "status", OrderStatus.PAID,
                "minId", minId,
                "maxId", maxId
            ))
            .pageSize(chunkSize)
            .build();
    }

    // 받은 범위를 Chunk 지향을 정산한다.
    @Bean
    public Step settlementWorkerStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        @Qualifier("settlementWorkerReader") JpaPagingItemReader<Order> settlementWorkerReader,
        OrderToSettlementProcessor settlementProcessor,
        JpaItemWriter<Settlement> settlementWriter) {
        return new StepBuilder("settlementWorkerReader", jobRepository)
            .<Order, Settlement>chunk(chunkSize)
            .reader(settlementWorkerReader)
            .processor(settlementProcessor) // Order를 Settlement로 변환
            .writer(settlementWriter)
            .transactionManager(transactionManager) // Chunk마다 커밋한다.
            .build();
    }
}
