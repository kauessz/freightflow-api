package com.freightflow.modules.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookRepository extends JpaRepository<WebhookSubscription, UUID> {

    /**
     * Returns all active subscriptions for the given tenant.
     * Uses path navigation: tenant.id — Spring Data JPA traverses the @ManyToOne.
     * Used by WebhookService to notify subscribers.
     */
    List<WebhookSubscription> findByTenant_IdAndActiveTrue(UUID tenantId);

    /**
     * Returns all subscriptions for a tenant (active and inactive).
     * Uses path navigation: tenant.id — Spring Data JPA traverses the @ManyToOne.
     * Used by WebhookService.list().
     */
    List<WebhookSubscription> findByTenant_Id(UUID tenantId);

    /**
     * Partial update — only the fields present in the UpdateWebhookRequest.
     * Uses JPQL to avoid requiring setters on the entity (entity is sealed from modification).
     * Null arguments for url, events, secret mean "keep existing value".
     */
    @Modifying
    @Query("UPDATE WebhookSubscription w SET " +
           "w.url    = CASE WHEN :url IS NOT NULL    THEN :url    ELSE w.url    END, " +
           "w.events = CASE WHEN :events IS NOT NULL THEN :events ELSE w.events END, " +
           "w.secret = CASE WHEN :secret IS NOT NULL THEN :secret ELSE w.secret END, " +
           "w.updatedAt = :now " +
           "WHERE w.id = :id")
    void updateFields(@Param("id")     UUID    id,
                      @Param("url")    String  url,
                      @Param("events") String  events,
                      @Param("secret") String  secret,
                      @Param("now")    Instant now);
}
