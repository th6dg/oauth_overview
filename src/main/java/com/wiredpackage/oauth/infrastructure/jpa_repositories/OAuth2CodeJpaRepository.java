package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.infrastructure.entities.OAuth2CodeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2CodeJpaRepository extends BaseJpaRepository<OAuth2CodeEntity, Long> {
    @Query("SELECT oc FROM OAuth2CodeEntity oc " +
        "WHERE oc.hashedCodeChallenge = :hashedCodeChallenge AND oc.codeChallenge = :codeChallenge ")
    Optional<OAuth2CodeEntity> findByCodeChallenge(@Param("hashedCodeChallenge") String hashedCodeChallenge,
                                                   @Param("codeChallenge") String codeChallenge);
}
