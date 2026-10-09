package com.school.erp.dto.homework;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkSubmissionRequest {

    private String submissionText;
    private String attachmentUrl;
    private String attachmentName;
}
