package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.api.application.models.auth.OAuth2GrantSummary;
import com.wiredpackage.oauth.api.application.models.auth.OAuthGrantSummary;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2GrantEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OAuth2GrantJpaRepository extends BaseJpaRepository<OAuth2GrantEntity, Long> {
    @Query(
        value = "SELECT EXISTS(" +
            "SELECT 1 " +
            "FROM oauth2_grants og " +
            "WHERE og.hashed_code_challenge = :hashedCodeChallenge AND og.code_challenge = :codeChallenge AND og.expires_at > :now " +
            ")", nativeQuery = true)
    int existsByCodeChallenge(@Param("codeChallenge") String codeChallenge,
                              @Param("hashedCodeChallenge") String hashedCodeChallenge,
                              @Param("now") LocalDateTime now);


    @Query(
        value = "SELECT og.id AS id, og.client_id AS clientId, og.code_challenge AS codeChallenge, og.expires_at AS " +
            "expiresAt, og.oauth_grant_type AS type, og.authentication_obj_id AS authenticationObjId, og.service_id as serviceId," +
            "og.location_id AS locationId, og.qr_invitation_code_id as qrInvitationCodeId, og.is_suite AS isSuite,  " +
            "og.start_current_recognition AS startCurrentRecognition, og.ai_time AS aiTime, og.recognize_time AS recognizeTime," +
            "og.recognize_count AS recognizeCount, og.end_current_recognition AS endCurrentRecognition, og.oauth_grant_type AS oauthGrantType, " +
            "og.start_current_verify AS startCurrentVerify, " +
            "og.customize_authentication_id as customizeAuthenticationId " +
            "FROM oauth2_grants og " +
            "WHERE og.hashed_code_challenge = :hashedCodeChallenge AND og.code_challenge = :codeChallenge AND og.expires_at > :now ", nativeQuery = true)
    Optional<OAuth2GrantSummary> findOAuth2GrantSummaryByCodeChallenge(@Param("codeChallenge") String codeChallenge,
                                                                       @Param("hashedCodeChallenge") String hashedCodeChallenge,
                                                                       @Param("now") LocalDateTime now);

    @Query(value = "SELECT og.id AS id, og.client_id AS clientId, og.code_challenge AS codeChallenge " +
        "FROM oauth2_grants og " +
        "WHERE og.id = :id", nativeQuery = true)
    Optional<OAuthGrantSummary> findOAuthGrantSummaryById(@Param("id") Long id);

    @Query(
        value = "SELECT og.* " +
        "FROM oauth2_grants og " +
        "INNER JOIN oauth2_logs ol ON ol.oauth2_grant_id = og.id " +
        "WHERE ol.id = :oauth2LogId", nativeQuery = true)
    Optional<OAuth2GrantEntity> findByOAuth2LogId(@Param("oauth2LogId") Long oAuth2LogId);

    @Query(
        value = "SELECT og.id AS id, og.client_id AS clientId, og.code_challenge AS codeChallenge, og.expires_at AS " +
            "expiresAt, og.oauth_grant_type AS type, og.authentication_obj_id AS authenticationObjId, og.service_id as serviceId," +
            "og.location_id AS locationId, og.qr_invitation_code_id as qrInvitationCodeId, og.is_suite AS isSuite, s.type AS serviceType, " +
            "og.customize_authentication_id as customizeAuthenticationId " +
            "FROM oauth2_grants og " +
            "JOIN services s ON og.service_id = s.id " +
            "WHERE og.hashed_code_challenge = :hashedCodeChallenge AND og.code_challenge = :codeChallenge AND og.expires_at > :now ", nativeQuery = true)
    Optional<OAuth2GrantSummary> findOAuth2GrantSummaryServiceTypeByCodeChallenge(@Param("codeChallenge") String codeChallenge,
                                                                                  @Param("hashedCodeChallenge") String hashedCodeChallenge,
                                                                                  @Param("now") LocalDateTime now);

    Optional<OAuth2GrantEntity> findByHashedCodeChallengeAndCodeChallengeAndExpiresAtAfter(String md5String, String codeChallenge,LocalDateTime now);

    Optional<OAuth2GrantEntity> findByHashedCodeChallengeAndCodeChallenge(String md5String, String codeChallenge);
}
