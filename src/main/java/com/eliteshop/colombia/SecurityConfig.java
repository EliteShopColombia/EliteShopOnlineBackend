package com.eliteshop.colombia;

import com.eliteshop.colombia.auth.infrastructure.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // CSRF disabled: This is a stateless REST API that authenticates via JWT Bearer tokens.
    // Session-based CSRF protection is not applicable — tokens are sent in the Authorization
    // header, not in cookies, making the application immune to cross-site request forgery attacks.
    // CORS is handled by the API Gateway (Go) — not here.
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/health",
                        "/webhooks/epayco/**",
                        "/api/v1/webhooks/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/sellers")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/products",
                        "/api/v1/products/{id}",
                        "/api/v1/products/images",
                        "/api/v1/reviews",
                        "/api/v1/reviews/{id}",
                        "/api/v1/reviews/product/{productId}",
                        "/api/v1/sellers",
                        "/api/v1/sellers/{id}",
                        "/api/v1/sellers/{id}/avatar",
                        "/api/v1/seller-contact/{sellerId}",
                        "/api/v1/customers/{id}/avatar")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/products")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/products/{id}")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/products/{id}")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/reviews")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/{id}")
                    .hasRole("CUSTOMER")
                    .requestMatchers(
                        "/api/v1/cart/**",
                        "/api/v1/checkout",
                        "/api/v1/payment-methods/**",
                        "/api/v1/payments/**")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/customers/{id}/avatar")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/customers/{id}/avatar")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/{id}/avatar")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/customers/{id}")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/{id}")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders/seller/**")
                    .hasRole("SELLER")
                    .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/v1/orders/*/prepare",
                        "/api/v1/orders/*/ship",
                        "/api/v1/orders/*/out-for-delivery",
                        "/api/v1/orders/*/tracking",
                        "/api/v1/orders/*/complete")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/orders/*/tracking")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/sellers/{id}/avatar")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/sellers/{id}/avatar")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/sellers/{id}/avatar")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/sellers/{id}")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/sellers/{id}")
                    .hasRole("SELLER")
                    .requestMatchers("/api/v1/seller-info/{sellerId}")
                    .hasRole("SELLER")
                    .requestMatchers("/api/v1/sellers/{sellerId}/verification/**")
                    .hasRole("SELLER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/orders")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/v1/orders/*/cancel",
                        "/api/v1/orders/*/confirm-delivery",
                        "/api/v1/orders/*/dispute",
                        "/api/v1/orders/*/refund")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/orders/{id}")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/{id}")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders/customer/**")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders/{id}")
                    .hasAnyRole("CUSTOMER", "SELLER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/customers")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/customers/{id}")
                    .hasRole("CUSTOMER")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
