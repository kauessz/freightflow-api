package com.freightflow.modules.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortRepository extends JpaRepository<Port, UUID> {

    Optional<Port> findByUnlocode(String unlocode);

    boolean existsByUnlocode(String unlocode);

    List<Port> findByCountryOrderByName(String country);

    @Query("""
        SELECT p FROM Port p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(p.unlocode) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY p.name
    """)
    List<Port> searchByNameOrUnlocode(@Param("query") String query);
}
