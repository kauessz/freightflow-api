package com.freightflow.modules.platform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, UUID> {

    @Query("""
        SELECT p FROM PlatformUser p
        WHERE lower(p.email) = lower(:email)
    """)
    Optional<PlatformUser> findByEmailIgnoreCase(@Param("email") String email);

    @Query("""
        SELECT COUNT(p) > 0 FROM PlatformUser p
        WHERE lower(p.email) = lower(:email)
    """)
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    Optional<PlatformUser> findFirstByOrderByCreatedAtAsc();
}
