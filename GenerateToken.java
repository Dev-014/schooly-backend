import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.nio.charset.StandardCharsets;

public class GenerateToken {
    public static void main(String[] args) {
        String secret = "change-me-change-me-change-me-change-me-change-me";
        String token = Jwts.builder()
            .setSubject("1")
            .claim("role", "SUPER_ADMIN")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
        System.out.println(token);
    }
}
