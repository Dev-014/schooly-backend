package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private School school;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "designation_id")
    private Long designationId;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "status")
    private String status;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "department")
    private String department;

    @Column(name = "designation")
    private String designation;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "biometric_id")
    private String biometricId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "work_experience")
    private String workExperience;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "work_shift")
    private String workShift;

    @Column(name = "location")
    private String location;

    @Column(name = "notes")
    private String notes;
}
