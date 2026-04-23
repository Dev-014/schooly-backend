package com.school.erp.api;

public record PaginationMeta(
        int page,
        int size,
        long total
) {
}
