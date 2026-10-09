package com.school.erp.dto.homework;

import com.school.erp.entity.homework.SubmissionStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkEvaluationRequest {

    private BigDecimal marksObtained;
    private String remarks;
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.EVALUATED;
    private String rubricScores;
}
