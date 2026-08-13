package com.eliteshop.colombia.shared.notification.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SlackMessageJpaRepository extends JpaRepository<SlackMessageEntity, UUID> {

  List<SlackMessageEntity> findByStatus(String status);

  @Modifying
  @Transactional
  @Query("UPDATE SlackMessageEntity s SET s.status = :status WHERE s.id = :id")
  void updateStatus(@Param("id") UUID id, @Param("status") String status);

  @Modifying
  @Transactional
  @Query(
      "UPDATE SlackMessageEntity s SET s.retryCount = s.retryCount + 1, s.nextRetryAt ="
          + " :nextRetryAt WHERE s.id = :id")
  void incrementRetryCount(
      @Param("id") UUID id, @Param("nextRetryAt") java.time.Instant nextRetryAt);
}
