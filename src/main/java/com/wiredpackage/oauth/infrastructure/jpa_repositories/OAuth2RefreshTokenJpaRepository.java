package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.infrastructure.entities.OAuth2RefreshTokenEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2RefreshTokenJpaRepository extends BaseJpaRepository<OAuth2RefreshTokenEntity, Long> {
    Optional<OAuth2RefreshTokenEntity> findByHashedRefreshTokenAndRefreshToken(@Param("hashedRefreshToken") String hashedRefreshToken,
                                                                               @Param("refreshToken") String refreshToken);
}
