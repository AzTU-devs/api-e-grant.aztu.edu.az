package az.aztu.egrant.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Issues and verifies JWTs. A single, consistent {@code role} claim is used throughout
 * (fixing the legacy {@code role} vs {@code role_code} mismatch).
 */
@Service
public class JwtService {

    private static final String CLAIM_FIN_KOD = "fin_kod";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_PROFILE_COMPLETED = "profile_completed";
    private static final String CLAIM_PURPOSE = "purpose";
    private static final String PURPOSE_OTP = "otp";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String finKod, String role, boolean profileCompleted) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_FIN_KOD, finKod)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_PROFILE_COMPLETED, profileCompleted)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenTtl())))
                .signWith(key)
                .compact();
    }

    /** Short-lived token proving OTP ownership; used for the password-reset step. */
    public String createOtpToken(String finKod) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(finKod)
                .claim(CLAIM_FIN_KOD, finKod)
                .claim(CLAIM_PURPOSE, PURPOSE_OTP)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.otpTokenTtl())))
                .signWith(key)
                .compact();
    }

    public Optional<AuthenticatedUser> parseAccessToken(String token) {
        try {
            Claims claims = parse(token);
            if (PURPOSE_OTP.equals(claims.get(CLAIM_PURPOSE, String.class))) {
                return Optional.empty();
            }
            Long userId = Long.valueOf(claims.getSubject());
            String finKod = claims.get(CLAIM_FIN_KOD, String.class);
            String role = claims.get(CLAIM_ROLE, String.class);
            Boolean profileCompleted = claims.get(CLAIM_PROFILE_COMPLETED, Boolean.class);
            return Optional.of(new AuthenticatedUser(
                    userId, finKod, role, Boolean.TRUE.equals(profileCompleted)));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /** Returns the FIN code carried by a valid OTP token. */
    public Optional<String> parseOtpToken(String token) {
        try {
            Claims claims = parse(token);
            if (!PURPOSE_OTP.equals(claims.get(CLAIM_PURPOSE, String.class))) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.get(CLAIM_FIN_KOD, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
