package az.aztu.egrant.shared.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-client-IP rate limiter (Bucket4j). Two bandwidths: per-day and per-hour, matching
 * the legacy Flask-Limiter defaults. In-memory; swap for a distributed store if scaled out.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.enabled()) {
            return true;
        }
        String path = request.getRequestURI();
        // Don't throttle docs/health probes.
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Bucket bucket = buckets.computeIfAbsent(clientIp(request), ip -> newBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/problem+json");
            response.getWriter().write(
                    "{\"type\":\"https://e-grant.aztu.edu.az/problems/rate-limit\","
                            + "\"title\":\"Too Many Requests\",\"status\":429}");
        }
    }

    private Bucket newBucket() {
        Bandwidth daily = Bandwidth.classic(properties.perDay(),
                Refill.intervally(properties.perDay(), Duration.ofDays(1)));
        Bandwidth hourly = Bandwidth.classic(properties.perHour(),
                Refill.intervally(properties.perHour(), Duration.ofHours(1)));
        return Bucket.builder().addLimit(daily).addLimit(hourly).build();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
