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
        WHERE u.email = :email
    """)
    Optional<User> findByEmailWithTenant(@Param("email") String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
