package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.api.application.models.auth.OAuthAuthenticationSummary;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2AuthenticationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OAuth2AuthenticationJpaRepository extends BaseJpaRepository<OAuth2AuthenticationEntity, Long> {
    @Query(value = "SELECT oa.id AS id, oa.identity_id AS identityId, oa.oauth2_grant_id AS oauth2GrantId, " +
        "oa.authentication_setting_id AS authenticationSettingId, oa.authentication_code AS authenticationCode, " +
        "oa.expires_at AS expiresAt " +
        "FROM oauth2_authentications oa " +
        "WHERE oa.hashed_authentication_code = :hashedCode AND oa.authentication_code = :code AND oa.expires_at > :expiresAt",
        nativeQuery = true)
    Optional<OAuthAuthenticationSummary> findOAuthAuthenticationSummaryByHashedCodeAndCodeAndExpiresBefore(@Param("hashedCode") String hashedCode,
                                                                                                           @Param("code") String code,
                                                                                                           @Param("expiresAt") LocalDateTime expiresAt);

    @Query(value = "SELECT COUNT(*) " +
        "FROM oauth2_authentications oa " +
        "WHERE oa.authentication_setting_id = :authenticationSettingId AND oa.created_at >= :from AND oa.created_at <= :to",
        nativeQuery = true)
    Long countOAuth2AuthenticationsByAuthenticationSettingIdAndTimeRange(@Param("authenticationSettingId") Long authenticationSettingId,
                                                                         @Param("from") LocalDateTime from,
                                                                         @Param("to") LocalDateTime to);

    Optional<OAuth2AuthenticationEntity> findTopByAuthenticationCode(String authenticationCode);

    Optional<OAuth2AuthenticationEntity> findTopByIdentityIdAndAuthenticationSettingIdOrderByCreatedAtDesc(
        Long identityId, Long authenticationSettingId);

    Optional<OAuth2AuthenticationEntity>  findFirstByOauth2GrantId(@Param("oauth2GrantId") Long oauth2GrantId);
}
