package com.growmighty.lectures.firstday.tangledmonolith.settlement.presentation;

import com.growmighty.lectures.firstday.tangledmonolith.common.response.ApiResponse;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.NaiveSettlementService;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.SettlementBatchService;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto.SettleReport;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 정산 실습용 트리거 엔드포인트의 모임
@RestController
@RequiredArgsConstructor
@RequestMapping("/settlements")
public class SettlementController {

    private static final long MB = 1024 * 1024;

    private final NaiveSettlementService naiveSettlementService;
    private final SettlementBatchService settlementBatchService;
    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;

    /**
     * [데모1/데모2] limit 이 없으면 findAll 전량 적재(OOM 데모),
     * limit 이 있으면 그 수만큼만 메모리에 쌓으며 정산(추세 관찰).
     */
    @PostMapping("/naive")
    public ApiResponse<SettleReport> settleNaive(@RequestParam(required = false) Integer limit) {
        SettleReport report = (limit == null)
            ? naiveSettlementService.settleAll()
            : naiveSettlementService.settleUpTo(limit);
        return ApiResponse.ok(report);
    }

    // failAt까지 정상 실행 후 강제 에러
    @PostMapping("/batch")
    public ApiResponse<SettleReport> settleBatch(@RequestParam(required = false) Double failAt) {
        SettleReport report = (failAt ==null)
            ? settlementBatchService.run()
            : settlementBatchService.runFailing(failAt);

        return ApiResponse.ok(report);
    }

    // 재시작 시 실패한 인스턴스부터 이어서 재개한다.
    @PostMapping("/batch/restart")
    public ApiResponse<SettleReport> restartBatch(@RequestParam(defaultValue = "1") long runId,
                                                  @RequestParam(required = false) Double failAt) {
        return ApiResponse.ok(settlementBatchService.runRestartable(runId, failAt));
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / MB;
        long maxMb = rt.maxMemory() / MB;
        return ApiResponse.ok(Map.of(
            "orderCount", orderRepository.count(),
            "settlementCount", settlementRepository.count(),
            "heapUsedMb", usedMb,
            "heapMaxMb", maxMb));
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        settlementRepository.deleteAll();
        return ApiResponse.ok();
    }
}
