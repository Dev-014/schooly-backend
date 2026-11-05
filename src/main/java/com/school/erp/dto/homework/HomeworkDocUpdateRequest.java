package com.school.erp.dto.homework;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkDocUpdateRequest {
    private UUID attachmentFileId;
    private String attachmentName;
    private String attachmentUrl;
}

