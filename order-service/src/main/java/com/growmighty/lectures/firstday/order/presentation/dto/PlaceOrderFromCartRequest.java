package com.growmighty.lectures.firstday.order.presentation.dto;

import lombok.NonNull;

public record PlaceOrderFromCartRequest(@NonNull Long userId) {
}
