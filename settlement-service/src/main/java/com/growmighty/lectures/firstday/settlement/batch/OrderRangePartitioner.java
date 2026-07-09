package com.growmighty.lectures.firstday.settlement.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

// 전체 주문을 여러 구간 (id 범위를 근거)으로 잘라서 각 Worker에게 나눠주는 역할
// gridSize로 나눈다.
@Slf4j
@RequiredArgsConstructor
public class OrderRangePartitioner implements Partitioner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Long min = jdbcTemplate.queryForObject(
            "SELECT MIN(id) FROM orders WHERE status = 'PAID'", Long.class
        );
        Long max = jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM orders WHERE status = 'PAID'", Long.class
        );

        // Spring Batch에게 전달할 파티션 목록
        Map<String, ExecutionContext> partitions = new HashMap<>();

        // 대상이 없으면 빈 파티션 1개 (minId > maxId인 경우. Reader가 읽은 것이 아무것도 없다.)
        if (min == null || max == null) {
            ExecutionContext empty = new ExecutionContext();
            empty.putLong("minId", 1L);
            empty.putLong("maxId", 0L);
            partitions.put("partition0", empty);
            log.warn("[PARTITION] 정산 대상 주문이 없습니다. 빈 파티션 1개 생성");
            return partitions;
        }

        long total = max - min + 1; // 전체 범위 계산
        long rangeSize = (long) Math.ceil((double) total / gridSize); // 파티션 당 Id의 폭 (한 파티션의 크기)

        long start = min;
        int index = 0;
        while (start <= max) {
            long end = Math.min(start + rangeSize - 1, max);

            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", start);
            context.putLong("maxId", end);
            partitions.put("partition" + index, context);
            log.warn("[PARTITION] partition{} -> id {} ~ {}", index, start, end);

            start = end + 1;
            index++;
        }

        log.warn("[PARTITION] 전체 id {} ~ {} 를 {}개 파티션으로 분할 (요청 gridSize = {}",
            min, max, partitions.size(), gridSize);
        return partitions;
    }
}
