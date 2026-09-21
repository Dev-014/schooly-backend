import os

entities = [
    "SchoolDepartment",
    "SchoolDesignation",
    "StaffAttendance",
    "SchoolLeaveType",
    "StaffLeave",
    "StaffLeaveBalance",
    "StaffPayrollDetails",
    "StaffAdvance",
    "StaffPayroll",
    "StaffTask",
    "StaffBankAccount",
    "StaffDocument",
    "RecruitmentCandidate"
]

base_dir = "/Users/admin/Desktop/Development/private/schooly-backend/src/main/java/com/school/erp/repository/hr"
os.makedirs(base_dir, exist_ok=True)

for entity in entities:
    content = f"""package com.school.erp.repository.hr;

import com.school.erp.entity.hr.{entity};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface {entity}Repository extends JpaRepository<{entity}, Long> {{
"""
    if entity == "RecruitmentCandidate":
        content += f"    List<{entity}> findBySchoolId(Long schoolId);\n"
    elif entity == "StaffBankAccount" or entity == "StaffPayrollDetails":
        content += f"    {entity} findByStaffId(Long staffId);\n"
    elif entity == "StaffDocument":
        content += f"    List<{entity}> findByStaffId(Long staffId);\n"
    elif "School" in entity and entity != "SchoolLeaveType": # SchoolDepartment, SchoolDesignation
        content += f"    List<{entity}> findBySchoolId(Long schoolId);\n"
    elif entity == "SchoolLeaveType":
        content += f"    List<{entity}> findBySchoolId(Long schoolId);\n"
    elif "Staff" in entity:
        content += f"    List<{entity}> findBySchoolId(Long schoolId);\n"
        content += f"    List<{entity}> findByStaffId(Long staffId);\n"
        if entity == "StaffAttendance":
            content += f"    java.util.Optional<{entity}> findByStaffIdAndAttendanceDate(Long staffId, java.time.LocalDate date);\n"
            content += f"    List<{entity}> findBySchoolIdAndAttendanceDate(Long schoolId, java.time.LocalDate date);\n"
        elif entity == "StaffLeaveBalance":
            content += f"    java.util.Optional<{entity}> findByStaffIdAndLeaveTypeId(Long staffId, Long leaveTypeId);\n"
        elif entity == "StaffPayroll":
            content += f"    java.util.Optional<{entity}> findByStaffIdAndPayrollMonthAndPayrollYear(Long staffId, String month, Integer year);\n"
            
    content += "}\n"
    
    with open(os.path.join(base_dir, f"{entity}Repository.java"), "w") as f:
        f.write(content)

print("Created repositories successfully.")
