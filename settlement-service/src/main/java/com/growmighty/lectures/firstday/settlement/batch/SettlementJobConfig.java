package com.growmighty.lectures.firstday.settlement.batch;

import com.growmighty.lectures.firstday.order.domain.Order;
import com.growmighty.lectures.firstday.order.domain.OrderStatus;
import com.growmighty.lectures.firstday.settlement.domain.Settlement;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;


@Configuration
public class SettlementJobConfig {

    public static final String JOB_NAME = "settlementJob";

    @Value("${settlement.batch.chunk-size:1000}")
    private int chunkSize;

    // pageSize를 chunkSize와 맞춰 한 페이지 = 한 청크 = 한 트랜잭션이 되게 한다.
    @Bean
    @StepScope
    public JpaPagingItemReader<Order> settlementOrderReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Order>()
            .name("settlementOrderReader")
            .entityManagerFactory(emf)
            .queryString("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.id ASC")
            .parameterValues(Map.of("status", OrderStatus.PAID))
            .pageSize(chunkSize)
            .build();
    }

    /**
     * [ItemWriter] 정산 엔티티를 chunk 단위로 적재.
     * (대용량 INSERT 최적화는 오후 세션에서 JdbcBatchItemWriter / Bulk Insert 로 다룬다.)
     */
    @Bean
    public JpaItemWriter<Settlement> settlementWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<Settlement>()
            .entityManagerFactory(emf)
            .build();
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               JpaPagingItemReader<Order> settlementOrderReader,
                               OrderToSettlementProcessor settlementProcessor,
                               JpaItemWriter<Settlement> settlementWriter) {
        return new StepBuilder("settlementStep", jobRepository)
            .<Order, Settlement>chunk(chunkSize)
            .reader(settlementOrderReader)
            .processor(settlementProcessor)
            .writer(settlementWriter)
            .transactionManager(transactionManager)
            .build();
    }

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(settlementStep)
            .build();
    }
}
