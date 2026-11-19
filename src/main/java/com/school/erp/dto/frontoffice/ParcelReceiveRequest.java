package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelReceiveRequest {

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @Pattern(regexp = "^$|^\\d{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNumber;

    private String recipientName;

    private String recipientType;

    @NotBlank(message = "Item details are required")
    private String itemDetails;

    @NotNull(message = "Date received is required")
    private LocalDate dateReceived;

    @NotBlank(message = "Received by name is required")
    private String receivedBy;

    private Long receivedByStaffId;
}
