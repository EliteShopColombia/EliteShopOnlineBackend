package com.eliteshop.colombia.seller.domain.model.verification;

import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SellerVerificationCreatedAt {
    private final Instant value;
}