package com.mza_agrotours.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse<T> {
    private boolean ok;
    private String code;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "ok", data);
    }

    public static <T> ApiResponse<T> fail(String code) {
        return new ApiResponse<>(false, code, null);
    }
}
