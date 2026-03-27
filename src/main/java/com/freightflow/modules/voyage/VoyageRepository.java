package com.freightflow.modules.voyage;

import com.freightflow.modules.voyage.enums.VoyageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoyageRepository extends JpaRepository<Voyage, UUID> {

    Optional<Voyage> findByVoyageNumber(String voyageNumber);

    boolean existsByVoyageNumber(String voyageNumber);

    @Query("""
        SELECT v FROM Voyage v
        JOIN FETCH v.vessel
        JOIN FETCH v.originPort
        JOIN FETCH v.destinationPort
        WHERE v.id = :id
    """)
    Optional<Voyage> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT v FROM Voyage v
        JOIN FETCH v.vessel
        JOIN FETCH v.originPort
        JOIN FETCH v.destinationPort
        WHERE v.voyageNumber = :voyageNumber
    """)
    Optional<Voyage> findByVoyageNumberWithDetails(@Param("voyageNumber") String voyageNumber);

    @Query(value = """
        SELECT v FROM Voyage v
        JOIN FETCH v.vessel
        JOIN FETCH v.originPort
        JOIN FETCH v.destinationPort
    """,
    countQuery = "SELECT COUNT(v) FROM Voyage v")
    Page<Voyage> findAllWithDetails(Pageable pageable);

    Page<Voyage> findByStatus(VoyageStatus status, Pageable pageable);
}
