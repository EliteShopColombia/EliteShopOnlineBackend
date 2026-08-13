package com.eliteshop.colombia.seller.infrastructure.adapter;

import com.eliteshop.colombia.seller.domain.model.verification.SellerVerification;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationId;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationRepository;
import com.eliteshop.colombia.seller.infrastructure.mapper.SellerVerificationMapper;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerVerificationEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerVerificationJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerVerificationPostgresAdapter implements SellerVerificationRepository {

    private final SellerVerificationJpaRepository jpaRepository;
    private final SellerVerificationMapper mapper;

    @Override
    public SellerVerification save(SellerVerification verification) {
        SellerVerificationEntity entity = mapper.toEntity(verification);
        SellerVerificationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SellerVerification> findById(SellerVerificationId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<SellerVerification> findBySellerId(UUID sellerId) {
        return jpaRepository.findBySellerId(sellerId).map(mapper::toDomain);
    }
}