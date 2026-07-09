package com.growmighty.lectures.firstday.order.application.dto;

import java.util.List;

public record OrderItemCommand(Long userId, List<OrderLine> lines){
}
