package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.api.application.models.authentication.*;
import com.wiredpackage.oauth.infrastructure.entities.AuthenticationSettingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthenticationSettingJpaRepository extends BaseJpaRepository<AuthenticationSettingEntity, Long> {
    @Query(value = "SELECT authS.id, authS.camera_name AS cameraName, authS.company_id AS companyId, authS.authentication_type_id AS typeId " +
        "FROM authentication_settings authS " +
        "INNER JOIN authentication_setting_items asi ON authS.id = asi.authentication_setting_id " +
        "WHERE asi.authentication_setting_field = :field AND asi.hashed_value = :hashedValue AND asi.value = :value " +
        "LIMIT 1", nativeQuery = true)
    Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryByFieldAndValue(@Param("field") String field,
                                                                                           @Param("value") String value,
                                                                                           @Param("hashedValue") String hashedValue);

    @Query(value = "SELECT authS.id, authS.camera_name AS cameraName, authS.company_id AS companyId, authS.authentication_type_id AS typeId " +
        "FROM authentication_settings authS " +
        "INNER JOIN authentication_types aut ON authS.authentication_type_id = aut.id " +
        "INNER JOIN plans p ON authS.id = p.authentication_camera_id AND p.is_deleted = FALSE " +
        "INNER JOIN companies c ON p.company_id = c.id AND c.is_deleted = false " +
        "INNER JOIN services s on p.service_id = s.id " +
        "WHERE s.type = :serviceType AND p.location_id = :locationId AND aut.type = :cameraType " +
        "LIMIT 1", nativeQuery = true)
    Optional<AuthenticationSettingSummary> findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(@Param("serviceType") String serviceType,
                                                                                                                   @Param("locationId") Long locationId,
                                                                                                                   @Param("cameraType") String cameraType);

    @Query(value = "SELECT asi.id AS id, asi.authentication_setting_field AS authenticationSettingField, " +
        "asi.authentication_setting_id AS authenticationSettingId, asi.value AS value " +
        "FROM authentication_settings authS " +
        "INNER JOIN authentication_setting_items asi ON authS.id = asi.authentication_setting_id " +
        "WHERE asi.authentication_setting_field = :field AND asi.authentication_setting_id = :authenticationSettingId " +
        "LIMIT 1", nativeQuery = true)
    Optional<AuthenticationSettingItem> findAuthenticationSettingItemByAuthenticationSettingIdAndField(
        @Param("authenticationSettingId") Long authenticationSettingId, @Param("field") String field);

    @Query(value = "SELECT asi.id AS id, asi.authentication_setting_field AS authenticationSettingField, " +
        "asi.authentication_setting_id AS authenticationSettingId, asi.value AS value " +
        "FROM authentication_setting_items asi " +
        "WHERE asi.authentication_setting_id = :authenticationSettingId ", nativeQuery = true)
    List<AuthenticationSettingItem> findAuthenticationSettingItemsByAuthenticationSettingId(@Param("authenticationSettingId") Long authenticationSettingId);

    @Query(value = "SELECT ai_settings.type AS type " +
        "FROM ai_settings " +
        "INNER JOIN authentication_ai_setting aas ON ai_settings.id = aas.ai_setting_id " +
        "WHERE aas.authentication_setting_id = :authenticationSettingId AND aas.is_manager_usage = true", nativeQuery = true)
    List<AuthenticationAiSetting> findAuthenticationAiSettingsByAuthenticationSettingId(@Param("authenticationSettingId") Long authenticationSettingId);

    @Query(value = "SELECT fd.type AS type " +
        "FROM face_directions fd " +
        "INNER JOIN authentication_face_direction afd ON fd.id = afd.face_direction_id " +
        "WHERE afd.authentication_setting_id = :authenticationSettingId ", nativeQuery = true)
    List<AuthenticationFaceDirection> findAuthenticationFaceDirectionsByAuthenticationSettingId(@Param("authenticationSettingId") Long authenticationSettingId);

    @Query(value = "SELECT asvt.type AS type " +
        "FROM two_step_verification_types asvt " +
        "WHERE asvt.id = :id ", nativeQuery = true)
    Optional<TwoStepVerificationType> findTwoStepVerificationTypeById(@Param("id") Long id);

    @Query(value = "SELECT authS.id, authS.camera_name AS cameraName, authS.company_id AS companyId, authS.authentication_type_id AS typeId " +
        "FROM authentication_settings authS " +
        "WHERE authS.id = :id ", nativeQuery = true)
    Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryById(@Param("id") Long id);

    @Query(value = "SELECT aws.id AS id, aws.url, aws.send_unregistered AS sendUnregistered, " +
        "aws.send_all_registered AS sendAllRegistered, aws.send_by_tags AS sendByTags, " +
        "IF (aws.use_token = TRUE, aws.token, NULL) AS token, " +
        "(SELECT GROUP_CONCAT(t.name SEPARATOR '~~~') FROM " +
            "authentication_webhook_setting_tags awst " +
            "JOIN tags t ON awst.tag_id = t.id " +
            "WHERE awst.authentication_webhook_setting_id = aws.id " +
            "GROUP BY awst.authentication_webhook_setting_id) AS tagNames " +
        "FROM authentication_webhook_settings aws " +
        "WHERE aws.authentication_setting_id = :id ", nativeQuery = true)
    List<AuthenticationWebhookSettingSummary> findAuthenticationWebhooksByAuthenticationSettingId(@Param("id") Long id);

    @Query(value = "SELECT awst.authentication_webhook_setting_id AS authenticationWebhookSettingId, " +
        "t.id AS tagId, t.name AS tagName " +
        "FROM authentication_webhook_setting_tags awst " +
        "JOIN tags t ON awst.tag_id = t.id " +
        "WHERE awst.authentication_webhook_setting_id IN (:webhookSettingIds) ", nativeQuery = true)
    List<AuthenticationWebhookTagSummary> findAuthenticationWebhookTagsByWebhookSettingId(
        @Param("webhookSettingIds") List<Long> webhookSettingIds);
}
