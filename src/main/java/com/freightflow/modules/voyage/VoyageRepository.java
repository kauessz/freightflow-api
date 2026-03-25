package com.freightflow.modules.voyage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoyageRepository extends JpaRepository<Voyage, UUID> {

    Optional<Voyage> findByVoyageNumber(String voyageNumber);

    boolean existsByVoyageNumber(String voyageNumber);
}
