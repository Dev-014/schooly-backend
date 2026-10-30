package com.school.erp.service.user;

import com.school.erp.dto.user.GrantAccessDTO;
import com.school.erp.dto.user.GrantAccessResponseDTO;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.auth.UserSchoolRoleRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.service.auth.RoleSyncService;
import com.school.erp.service.superadmin.UserSupportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccessServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserSchoolRoleRepository userSchoolRoleRepository;

    @Mock
    private UserSupportService userSupportService;

    @Mock
    private RoleSyncService roleSyncService;

    @InjectMocks
    private UserAccessService userAccessService;

    private School testSchool;
    private Student testStudent;
    private User testUser;

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setName("Demo School");

        testStudent = new Student();
        testStudent.setId(10L);
        testStudent.setName("Aarav Sharma");
        testStudent.setAdmissionNo("ADM-10045");
        testStudent.setSchool(testSchool);

        testUser = new User();
        testUser.setId(100L);
        testUser.setName("Aarav Sharma");
        testUser.setEmail("aaravsharma10045@schooly.internal");
        Map<String, Object> meta = new HashMap<>();
        meta.put("tempPassword", "TempPass@123");
        meta.put("username", "ADM-10045");
        testUser.setMetadata(meta);
    }

    @Test
    @DisplayName("grantAccess creates new credentials when student has no linked user")
    void grantAccess_NewUser() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(testStudent));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });

        GrantAccessDTO dto = new GrantAccessDTO();
        dto.setEntityType("STUDENT");
        dto.setEntityId(10L);

        GrantAccessResponseDTO response = userAccessService.grantAccess(dto);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getGeneratedUsername()).isEqualTo("ADM-10045");
        assertThat(response.getGeneratedPassword()).isNotEmpty();
        verify(studentRepository).save(testStudent);
    }

    @Test
    @DisplayName("grantAccess returns existing credentials when student already has linked user")
    void grantAccess_ExistingUser() {
        testStudent.setUserId(100L);
        when(studentRepository.findById(10L)).thenReturn(Optional.of(testStudent));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        GrantAccessDTO dto = new GrantAccessDTO();
        dto.setEntityType("STUDENT");
        dto.setEntityId(10L);

        GrantAccessResponseDTO response = userAccessService.grantAccess(dto);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getGeneratedUsername()).isEqualTo("ADM-10045");
        assertThat(response.getGeneratedPassword()).isEqualTo("TempPass@123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getCredentials retrieves student user credentials")
    void getCredentials_Success() {
        testStudent.setUserId(100L);
        when(studentRepository.findById(10L)).thenReturn(Optional.of(testStudent));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        GrantAccessResponseDTO response = userAccessService.getCredentials("STUDENT", 10L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getGeneratedUsername()).isEqualTo("ADM-10045");
        assertThat(response.getGeneratedPassword()).isEqualTo("TempPass@123");
    }

    @Test
    @DisplayName("resetCredentials updates password and returns new temp credentials")
    void resetCredentials_Success() {
        testStudent.setUserId(100L);
        when(studentRepository.findById(10L)).thenReturn(Optional.of(testStudent));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        GrantAccessResponseDTO response = userAccessService.resetCredentials("STUDENT", 10L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getGeneratedUsername()).isEqualTo("ADM-10045");
        assertThat(response.getGeneratedPassword()).isNotNull();
        verify(userRepository).save(testUser);
    }
}
