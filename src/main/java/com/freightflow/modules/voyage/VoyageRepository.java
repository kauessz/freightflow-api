package com.freightflow.modules.voyage;

import com.freightflow.modules.voyage.enums.VoyageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Voyages ativas (IN_TRANSIT ou DEPARTED) que contêm pelo menos um embarque
     * do tenant informado. Usado no endpoint GET /vessels/active-with-shipments.
     *
     * DISTINCT evita duplicatas quando a voyage tem múltiplos shipments do mesmo tenant.
     */
    @Query("""
        SELECT DISTINCT v FROM Voyage v
        JOIN FETCH v.vessel
        JOIN FETCH v.originPort
        JOIN FETCH v.destinationPort
        JOIN v.shipments s
        WHERE v.status IN :statuses
          AND s.tenant.id = :tenantId
    """)
    List<Voyage> findActiveVoyagesWithTenantShipments(
            @Param("tenantId") UUID tenantId,
            @Param("statuses")  List<VoyageStatus> statuses);

    @Query("""
        SELECT DISTINCT v FROM Voyage v
        JOIN FETCH v.vessel
        JOIN FETCH v.originPort
        JOIN FETCH v.destinationPort
        JOIN v.shipments s
        WHERE v.status IN :statuses
          AND s.tenant.id = :tenantId
          AND s.customer.id = :customerId
    """)
    List<Voyage> findActiveVoyagesWithCustomerShipments(
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId,
            @Param("statuses") List<VoyageStatus> statuses);
}
