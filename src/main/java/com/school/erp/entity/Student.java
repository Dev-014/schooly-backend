package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "admission_no", nullable = false)
    private String admissionNo;

    @Column(name = "roll_number")
    private String rollNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "academic_year_id")
    private Long academicYearId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "religion")
    private String religion;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_relation")
    private String guardianRelation;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(name = "guardian_occupation")
    private String guardianOccupation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private StudentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private StudentHouse house;
}
