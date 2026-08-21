package com.eliteshop.colombia.auth.infrastructure.filter;

import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    try {
      Claims claims = jwtService.validateToken(token);

      String userId = claims.getSubject();
      String email = claims.get("email", String.class);
      String role = claims.get("role", String.class);
      String sellerId = claims.get("sellerId", String.class);

      request.setAttribute("gateway.userId", userId);
      request.setAttribute("gateway.userEmail", email);
      request.setAttribute("gateway.userRole", role);
      if (sellerId != null) {
        request.setAttribute("gateway.sellerId", sellerId);
      }

      SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of(authority));
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (JwtException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response
          .getWriter()
          .write("{\"error\":\"unauthorized\",\"message\":\"Invalid or expired token\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
