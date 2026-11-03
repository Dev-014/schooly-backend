package com.school.erp.repository.finance;

import com.school.erp.entity.finance.FeeStructureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStructureItemRepository extends JpaRepository<FeeStructureItem, Long> {
    List<FeeStructureItem> findByFeeStructureId(Long feeStructureId);

    @Query("""
            select fc.name, coalesce(sum(fsi.amount), 0)
            from FeeStructureItem fsi
            join fsi.feeStructure fs
            join fsi.feeCategory fc
            where fs.school.id = :schoolId
            group by fc.name
            order by sum(fsi.amount) desc
            """)
    List<Object[]> sumAmountsByCategoryAndSchoolId(@Param("schoolId") Long schoolId);
}
