package com.school.erp.dto.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SendFeeReminderRequest {
    @NotEmpty(message = "Student IDs cannot be empty")
    private List<Long> studentIds;

    @NotNull(message = "Method is required")
    private String method; // APP, SMS, WHATSAPP
}
