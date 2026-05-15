package com.artecomcarinho.security;

import com.artecomcarinho.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {

    private final Map<String, AttemptBucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup = new AtomicLong(0);

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.security.rate-limit.trust-forwarded-headers:false}")
    private boolean trustForwardedHeaders;

    public void check(String bucketName, String key, int maxAttempts, Duration window) {
        if (!enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        String bucketKey = normalize(bucketName) + ":" + normalize(key);
        AttemptBucket bucket = buckets.computeIfAbsent(bucketKey, ignored -> new AttemptBucket());

        synchronized (bucket) {
            prune(bucket.attempts, now, window);
            if (bucket.attempts.size() >= maxAttempts) {
                throw new TooManyRequestsException("Muitas tentativas. Aguarde um pouco e tente novamente.");
            }
            bucket.attempts.addLast(now);
        }

        cleanupOldBuckets(now, window);
    }

    public String key(HttpServletRequest request, String discriminator) {
        String ip = resolveClientIp(request);
        if (discriminator == null || discriminator.isBlank()) {
            return ip;
        }
        return ip + ":" + discriminator;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        if (trustForwardedHeaders) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }

            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }

    private void cleanupOldBuckets(long now, Duration window) {
        long previousCleanup = lastCleanup.get();
        if (now - previousCleanup < Duration.ofMinutes(5).toMillis()
                || !lastCleanup.compareAndSet(previousCleanup, now)) {
            return;
        }

        buckets.entrySet().removeIf(entry -> {
            AttemptBucket bucket = entry.getValue();
            synchronized (bucket) {
                prune(bucket.attempts, now, window);
                return bucket.attempts.isEmpty();
            }
        });
    }

    private void prune(Deque<Long> attempts, long now, Duration window) {
        long earliestAllowed = now - window.toMillis();
        while (!attempts.isEmpty() && attempts.peekFirst() < earliestAllowed) {
            attempts.removeFirst();
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static class AttemptBucket {
        private final Deque<Long> attempts = new ArrayDeque<>();
    }
}
