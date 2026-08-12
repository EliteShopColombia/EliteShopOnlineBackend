package com.eliteshop.colombia.review.infrastructure.config;

import com.eliteshop.colombia.review.application.usecase.*;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewBeanConfiguration {

    @Bean
    public ReviewSaveUseCase reviewSaveUseCase(ReviewRepository reviewRepository) {
        return new ReviewSaveUseCase(reviewRepository);
    }

    @Bean
    public ReviewDeleteUseCase reviewDeleteUseCase(ReviewRepository reviewRepository) {
        return new ReviewDeleteUseCase(reviewRepository);
    }

    @Bean
    public ReviewFindAllUseCase reviewFindAllUseCase(ReviewRepository reviewRepository) {
        return new ReviewFindAllUseCase(reviewRepository);
    }

    @Bean
    public ReviewFindByIdUseCase reviewFindByIdUseCase(ReviewRepository reviewRepository) {
        return new ReviewFindByIdUseCase(reviewRepository);
    }

    @Bean
    public ReviewFindByProductIdUseCase reviewFindByProductIdUseCase(ReviewRepository reviewRepository) {
        return new ReviewFindByProductIdUseCase(reviewRepository);
    }
}