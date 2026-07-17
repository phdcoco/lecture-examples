package com.growmighty.lectures.firstday.common.exception;

// 400, 500도 아니고, 잠시 후에 될 거야 라는 503 던짐.
public class ServiceUnavailableException  extends BusinessException{

    public ServiceUnavailableException(String message) {
        super(ErrorCode.SERVICE_UNAVAILABLE, message);
    }
}
