package com.school.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StudentParentId implements Serializable {

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "parent_user_id")
    private Long parentUserId;

    public StudentParentId() {
    }

    public StudentParentId(Long studentId, Long parentUserId) {
        this.studentId = studentId;
        this.parentUserId = parentUserId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getParentUserId() {
        return parentUserId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof StudentParentId that)) {
            return false;
        }
        return Objects.equals(studentId, that.studentId) && Objects.equals(parentUserId, that.parentUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, parentUserId);
    }
}
