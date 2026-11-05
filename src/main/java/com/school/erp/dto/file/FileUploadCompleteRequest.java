package com.school.erp.dto.file;

public record FileUploadCompleteRequest(
        String checksumSha256
) {
    public FileUploadCompleteRequest() {
        this(null);
    }
}

