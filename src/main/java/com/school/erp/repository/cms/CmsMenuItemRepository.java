package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsMenuItemRepository extends JpaRepository<CmsMenuItem, Long> {

    List<CmsMenuItem> findByMenuGroupIdOrderBySortOrderAscIdAsc(Long menuGroupId);

    void deleteByMenuGroupId(Long menuGroupId);
}

