package com.eliteshop.colombia.seller.domain.model.verification;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SellerVerificationId {
    private final UUID value;

    public SellerVerificationId(UUID value) {
        Objects.requireNonNull(value, "El ID de verificacion no puede ser nulo");
        this.value = value;
    }

    public static SellerVerificationId generate() {
        return new SellerVerificationId(UUID.randomUUID());
    }
}