package com.freightflow.modules.platform.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformFeatureRepository extends JpaRepository<PlatformFeature, String> {

    Page<PlatformFeature> findByActive(boolean active, Pageable pageable);

    Page<PlatformFeature> findByValueType(PlatformFeatureValueType valueType, Pageable pageable);

    Page<PlatformFeature> findByActiveAndValueType(boolean active, PlatformFeatureValueType valueType, Pageable pageable);
}
