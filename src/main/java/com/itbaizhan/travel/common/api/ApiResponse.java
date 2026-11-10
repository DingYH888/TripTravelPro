package com.itbaizhan.travel.common.api;

import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一 API 响应体。
 */
@Data
public class ApiResponse<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = MDC.get("traceId");
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "OK", null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
