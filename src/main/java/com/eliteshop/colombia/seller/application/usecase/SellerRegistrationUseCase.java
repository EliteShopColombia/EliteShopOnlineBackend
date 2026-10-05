package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.AuthResponse;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.shared.exception.ResourceAccessDeniedException;
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

  /**
   * Registra un vendedor para la cuenta autenticada.
   *
   * <p>El {@code customerId} que va como subject del JWT es SIEMPRE el del usuario autenticado; nunca
   * se deriva del email enviado en el body. Además, el email del vendedor debe coincidir con el de
   * la cuenta autenticada, de modo que nadie pueda emitirse un token con la identidad de otro
   * cliente conociendo su email.
   */
  public AuthResponse execute(Seller seller, UUID authenticatedCustomerId, String authenticatedEmail) {
    String email = seller.getContact().getEmail().getValue();
    log.info(
        "Registrando vendedor para customerId={}, email={}", authenticatedCustomerId, email);

    if (authenticatedCustomerId == null) {
      throw new ResourceAccessDeniedException("La autenticación es obligatoria para registrar un vendedor");
    }

    if (authenticatedEmail == null || !authenticatedEmail.equalsIgnoreCase(email)) {
      log.warn(
          "Rechazado registro de vendedor con email distinto al autenticado: autenticado={}, solicitado={}",
          authenticatedEmail,
          email);
      throw new ResourceAccessDeniedException(
          "El email del vendedor debe coincidir con el de la cuenta autenticada");
    }

    saveUseCase.execute(seller);

    String customerId = authenticatedCustomerId.toString();
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
