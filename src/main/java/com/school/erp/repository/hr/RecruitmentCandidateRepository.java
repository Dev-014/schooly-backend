package com.school.erp.repository.hr;

import com.school.erp.entity.hr.RecruitmentCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentCandidateRepository extends JpaRepository<RecruitmentCandidate, Long> {
    List<RecruitmentCandidate> findBySchoolId(Long schoolId);
}
