package com.wiredpackage.oauth.api.application.queries.authentication;

import com.wiredpackage.oauth.api.application.models.authentication.*;

import java.util.List;
import java.util.Optional;

public interface IAuthenticationSettingQueriesService {
    Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryByFieldAndValue(String field, String value);

    Optional<AuthenticationSettingItem> findAuthenticationSettingItemByAuthenticationSettingIdAndField(
        Long authenticationSettingId, String field);

    List<AuthenticationSettingItem> findAuthenticationSettingItemsByAuthenticationSettingId(Long authenticationSettingId);

    List<AuthenticationAiSetting> findAuthenticationAiSettingsByAuthenticationSettingId(Long authenticationSettingId);

    List<AuthenticationFaceDirection> findAuthenticationFaceDirectionsByAuthenticationSettingId(Long authenticationSettingId);

    Optional<TwoStepVerificationType> findTwoStepVerificationTypeById(Long id);

    Optional<AuthenticationSettingSummary> findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(String serviceType,
                                                                                                            Long locationId, String cameraType);

    Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryById(Long id);

    List<AuthenticationWebhookSettingSummary> findAuthenticationWebhooksByAuthenticationSettingId(Long id);

    List<AuthenticationWebhookTagSummary> findAuthenticationWebhookTagsByWebhookSettingId(List<Long> webhookSettingIds);
}
