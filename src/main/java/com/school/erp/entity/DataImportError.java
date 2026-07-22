package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "data_import_errors")
public class DataImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Long errorId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "row_index", nullable = false)
    private String rowIndex;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "current_value")
    private String currentValue;

    @Column(name = "resolved")
    private Boolean resolved = false;
}
