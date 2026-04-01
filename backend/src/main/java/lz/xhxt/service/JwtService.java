package lz.xhxt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 签发与解析
 */
@Component
public class JwtService {

    @Value("${xhxt.jwt.secret}")
    private String secret;

    @Value("${xhxt.jwt.expire-minutes:120}")
    private long expireMinutes;

    public String sign(Long userId, String account, String role) {
        Date exp = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("account", account)
                .withClaim("role", role)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    public Long parseUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        String pure = token.startsWith("Bearer ") ? token.substring(7) : token;
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        DecodedJWT jwt = verifier.verify(pure);
        String sub = jwt.getSubject();
        return sub == null ? null : Long.parseLong(sub);
    }
}
