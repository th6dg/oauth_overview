package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.api.application.models.auth.IdentityRole;
import com.wiredpackage.oauth.infrastructure.entities.RoleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleJpsRepository extends BaseJpaRepository<RoleEntity, Long> {
    @Query(value = "SELECT r.name AS name, r.type AS `type` " +
        "FROM roles r " +
        "WHERE r.id = :id", nativeQuery = true)
    Optional<IdentityRole> findRoleById(@Param("id") Long id);
}
