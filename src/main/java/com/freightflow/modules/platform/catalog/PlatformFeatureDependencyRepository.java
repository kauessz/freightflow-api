package com.freightflow.modules.platform.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PlatformFeatureDependencyRepository
        extends JpaRepository<PlatformFeatureDependency, PlatformFeatureDependencyId> {

    @Query("""
            select d
            from PlatformFeatureDependency d
            join fetch d.feature f
            join fetch d.requiredFeature rf
            where d.feature.key in :featureKeys
            order by f.key asc, rf.key asc
            """)
    List<PlatformFeatureDependency> findAllByFeatureKeys(Collection<String> featureKeys);

    @Query("""
            select d
            from PlatformFeatureDependency d
            join fetch d.feature f
            join fetch d.requiredFeature rf
            order by f.key asc, rf.key asc
            """)
    List<PlatformFeatureDependency> findAllWithRequiredFeature();
}
