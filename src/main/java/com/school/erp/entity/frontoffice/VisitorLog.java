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
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "visitor_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "visitor_name", nullable = false)
    private String visitorName;

    @Column(name = "purpose", nullable = false, length = 100)
    private String purpose;

    @Column(name = "meeting_with")
    private String meetingWith;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_with_staff_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff meetingWithStaff;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "number_of_people")
    private Integer numberOfPeople = 1;

    @Column(name = "id_card_number", length = 100)
    private String idCardNumber;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "time_in", nullable = false)
    private LocalTime timeIn;

    @Column(name = "est_time_out")
    private LocalTime estTimeOut;

    @Column(name = "time_out")
    private LocalTime timeOut;

    @Column(name = "status", length = 50)
    private String status = "ON_SITE";

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
