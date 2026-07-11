package com.growmighty.lectures.firstday.order.presentation;


import com.growmighty.lectures.firstday.common.response.ApiResponse;
import com.growmighty.lectures.firstday.order.application.OrderApiService;
import com.growmighty.lectures.firstday.order.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    OrderService, OrderFacade 삭제.
    OrderService용 엔드포인트가 담긴 기존 OrderController 삭제
    우리는 서비스끼리는 HTTP로만 대화한다.
    따라서 implementation으로 product-service를 추가하는 방법은 옳지 않다.
    여기서만 orderApiService로 대화할 것이다.
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderApiService orderApiService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrders() {
        List<OrderResponse> responses = orderApiService.getOrders().stream()
            .map(OrderResponse::from)
            .toList();
        return ApiResponse.ok(responses);
    }

    @PostMapping
    public ApiResponse<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        return ApiResponse.ok(OrderResponse.from(orderApiService.placeOrder(request.toCommand())));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(OrderResponse.from(orderApiService.cancelOrder(orderId)));
    }

    @GetMapping("/{orderId}/inspect")
    public ApiResponse<OrderConsistencyResponse> inspectOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(OrderConsistencyResponse.from(orderApiService.inspectOrder(orderId)));
    }

    @PatchMapping("/{orderId}/orderItems/{orderItemId}/price")
    public ApiResponse<Void> changeOrderItemPrice(@PathVariable Long orderId, @PathVariable Long orderItemId, @RequestBody ChangeOrderItemPriceRequest request) {
        orderApiService.changeItemPrice(orderId, orderItemId, request.price());
        return ApiResponse.ok();
    }

    @PatchMapping("/{orderId}/orderItems/{orderItemId}/quantity")
    public ApiResponse<Void> changeOrderItemQuantity(@PathVariable Long orderId, @PathVariable Long orderItemId, @RequestBody ChangeOrderItemQuantityRequest request) {
        orderApiService.changeItemQuantity(orderId, orderItemId, request.quantity());
        return ApiResponse.ok();
    }
}
