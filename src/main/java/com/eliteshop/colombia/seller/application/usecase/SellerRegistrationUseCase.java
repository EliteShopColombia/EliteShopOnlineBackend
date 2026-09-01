package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.AuthResponse;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.seller.domain.model.Seller;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerRegistrationUseCase {

  private final SellerSaveUseCase saveUseCase;
  private final JwtService jwtService;
  private final CustomerRepository customerRepository;

  public AuthResponse execute(Seller seller) {
    log.info("Registrando vendedor: {}", seller.getContact().getEmail().getValue());
    saveUseCase.execute(seller);

    String email = seller.getContact().getEmail().getValue();
    String customerId =
        customerRepository
            .findByEmail(email)
            .map(c -> c.getId().getValue().toString())
            .orElse(seller.getId().getValue().toString());

    String token =
        jwtService.generateToken(customerId, email, "seller", seller.getId().getValue().toString());

    AuthResponse.UserInfo userInfo =
        new AuthResponse.UserInfo(
            UUID.fromString(customerId),
            email,
            seller.getFullname().getValue(),
            seller.getFullname().getValue(),
            "seller");

    log.info("Vendedor registrado exitosamente: {}", seller.getId().getValue());
    return new AuthResponse(token, jwtService.getExpiration(), userInfo);
  }
}
