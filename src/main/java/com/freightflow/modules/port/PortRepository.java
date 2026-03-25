package com.freightflow.modules.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortRepository extends JpaRepository<Port, UUID> {

    Optional<Port> findByUnlocode(String unlocode);

    boolean existsByUnlocode(String unlocode);
}
