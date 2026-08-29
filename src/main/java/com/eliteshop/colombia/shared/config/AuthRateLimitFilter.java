package com.eliteshop.colombia.shared.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Profile("!test")
public class AuthRateLimitFilter extends OncePerRequestFilter {

  private final RateLimiter loginRateLimiter;
  private final RateLimiter registerRateLimiter;

  public AuthRateLimitFilter() {
    this.loginRateLimiter =
        RateLimiter.of(
            "auth-login",
            RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build());

    this.registerRateLimiter =
        RateLimiter.of(
            "auth-register",
            RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();

    if ("POST".equals(method)) {
      RateLimiter rateLimiter = null;

      if ("/api/v1/auth/login".equals(path)) {
        rateLimiter = loginRateLimiter;
      } else if ("/api/v1/auth/register".equals(path)) {
        rateLimiter = registerRateLimiter;
      }

      if (rateLimiter != null && !rateLimiter.acquirePermission()) {
        log.warn("Rate limit excedido para {} desde {}", path, request.getRemoteAddr());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response
            .getWriter()
            .write(
                "{\"status\":423,\"error\":\"Demasiadas solicitudes\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
