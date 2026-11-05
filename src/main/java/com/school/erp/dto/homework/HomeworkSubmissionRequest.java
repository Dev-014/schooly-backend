package com.school.erp.dto.homework;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkSubmissionRequest {

    private String submissionText;
    private String attachmentUrl;
    private String attachmentName;
    private UUID attachmentFileId;
}
