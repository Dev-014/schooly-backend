import com.school.erp.security.JwtUtil;
import com.school.erp.dto.usermanagement.UserRole;
public class TestToken {
    public static void main(String[] args) {
        JwtUtil jwtUtil = new JwtUtil("this-is-a-super-secret-key-that-should-be-at-least-256-bits-long-for-hmac-sha", 3600000, 7200000);
        String token = jwtUtil.generateAccessToken(34L, null, UserRole.SUPER_ADMIN, null);
        System.out.println(token);
    }
}
