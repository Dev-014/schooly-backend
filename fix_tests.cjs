const fs = require('fs');

let authServiceTest = fs.readFileSync('src/test/java/com/school/erp/service/AuthServiceTest.java', 'utf8');
authServiceTest = authServiceTest.replace(
  'import org.mockito.MockitoAnnotations;',
  'import org.mockito.MockitoAnnotations;\nimport com.school.erp.service.auth.AuthorizationService;'
);
authServiceTest = authServiceTest.replace(
  '@Mock\n    private JwtUtil jwtUtil;',
  '@Mock\n    private JwtUtil jwtUtil;\n\n    @Mock\n    private AuthorizationService authorizationService;'
);
authServiceTest = authServiceTest.replace(
  'new AuthService(userRepository, userSchoolRoleRepository, authSessionRepository, studentParentRepository, schoolRepository, studentRepository, jwtUtil);',
  'new AuthService(userRepository, userSchoolRoleRepository, authSessionRepository, studentParentRepository, schoolRepository, studentRepository, jwtUtil, authorizationService);'
);
fs.writeFileSync('src/test/java/com/school/erp/service/AuthServiceTest.java', authServiceTest);


let authControllerTest = fs.readFileSync('src/test/java/com/school/erp/controller/AuthControllerTest.java', 'utf8');
authControllerTest = authControllerTest.replace(
  'new AuthTokenResponse(10L, 101L, "ADMIN", "mock-access-token", "mock-refresh-token")',
  'new AuthTokenResponse(10L, 101L, "ADMIN", "mock-access-token", "mock-refresh-token", java.util.List.of("ALL"))'
);
fs.writeFileSync('src/test/java/com/school/erp/controller/AuthControllerTest.java', authControllerTest);

console.log('Fixed tests');
