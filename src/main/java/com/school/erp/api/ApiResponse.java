package com.school.erp.api;

public record ApiResponse<T>(
        String status,
        T data,
        String message,
        PaginationMeta pagination
) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message, null);
    }

    public static <T> ApiResponse<T> success(T data, String message, PaginationMeta pagination) {
        return new ApiResponse<>("success", data, message, pagination);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message, null);
    }
}
