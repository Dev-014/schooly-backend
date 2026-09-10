package com.school.erp.entity.frontoffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "parcel_receives")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ParcelReceive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "contact_number", length = 50)
    private String contactNumber;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_type", length = 50)
    private String recipientType;

    @Column(name = "item_details", nullable = false, columnDefinition = "TEXT")
    private String itemDetails;

    @Column(name = "date_received", nullable = false)
    private LocalDate dateReceived;

    @Column(name = "received_by", nullable = false)
    private String receivedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_staff_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff receivedByStaff;

    @Column(name = "status", length = 50)
    private String status = "RECEIVED"; // RECEIVED, COLLECTED, RETURNED

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "collected_by")
    private String collectedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
