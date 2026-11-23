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
public class ParcelDispatchRequest {

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    private String receiverInstitution;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @Pattern(regexp = "^$|^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotNull(message = "Dispatch date is required")
    private LocalDate dispatchDate;

    @NotBlank(message = "Item details are required")
    private String itemDetails;

    @NotBlank(message = "Courier name is required")
    private String courierName;

    private String trackingNumber;

    private String notes;
}
