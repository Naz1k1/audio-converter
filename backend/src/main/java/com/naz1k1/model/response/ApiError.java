package com.naz1k1.model.response;

import lombok.Data;

@Data
public class ApiError {
    private final String error;
    private final String message;
    private final Long timestamp;

    public ApiError(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
