package com.freightflow.modules.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.tenant
        LEFT JOIN FETCH u.customer
        WHERE u.email = :email
    """)
    Optional<User> findByEmailWithTenant(@Param("email") String email);

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.tenant
        LEFT JOIN FETCH u.customer
        WHERE u.id = :id
    """)
    Optional<User> findByIdWithDetails(@Param("id") UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    org.springframework.data.domain.Page<User> findByTenantId(UUID tenantId,
            org.springframework.data.domain.Pageable pageable);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
}
