package com.school.erp.dto.file;

import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;

public record FileFilterRequest(
        FileEntityType entityType,
        Long entityId,
        FileCategory category,
        FileStatus status,
        Integer page,
        Integer size
) {}

