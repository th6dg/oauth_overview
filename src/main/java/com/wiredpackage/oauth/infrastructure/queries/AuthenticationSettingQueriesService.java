package com.wiredpackage.oauth.infrastructure.queries;

import com.wiredpackage.oauth.api.application.models.authentication.*;
import com.wiredpackage.oauth.api.application.queries.authentication.IAuthenticationSettingQueriesService;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.AuthenticationSettingJpaRepository;
import com.wiredpackage.shared.shared.utils.HashUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class AuthenticationSettingQueriesService implements IAuthenticationSettingQueriesService {
    private final AuthenticationSettingJpaRepository authenticationSettingJpaRepository;

    @Override
    public Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryByFieldAndValue(String field, String value) {
        return authenticationSettingJpaRepository.findAuthenticationSettingSummaryByFieldAndValue(field, value, HashUtils.md5String(value));
    }

    @Override
    public Optional<AuthenticationSettingItem> findAuthenticationSettingItemByAuthenticationSettingIdAndField(Long authenticationSettingId, String field) {
        return authenticationSettingJpaRepository.findAuthenticationSettingItemByAuthenticationSettingIdAndField(authenticationSettingId, field);
    }

    @Override
    public List<AuthenticationSettingItem> findAuthenticationSettingItemsByAuthenticationSettingId(Long authenticationSettingId) {
        return authenticationSettingJpaRepository.findAuthenticationSettingItemsByAuthenticationSettingId(authenticationSettingId);
    }

    @Override
    public List<AuthenticationAiSetting> findAuthenticationAiSettingsByAuthenticationSettingId(Long authenticationSettingId) {
        return authenticationSettingJpaRepository.findAuthenticationAiSettingsByAuthenticationSettingId(authenticationSettingId);
    }

    @Override
    public List<AuthenticationFaceDirection> findAuthenticationFaceDirectionsByAuthenticationSettingId(Long authenticationSettingId) {
        return authenticationSettingJpaRepository.findAuthenticationFaceDirectionsByAuthenticationSettingId(authenticationSettingId);
    }

    @Override
    public Optional<TwoStepVerificationType> findTwoStepVerificationTypeById(Long id) {
        return authenticationSettingJpaRepository.findTwoStepVerificationTypeById(id);
    }

    @Override
    public Optional<AuthenticationSettingSummary> findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(String serviceType,
                                                                                                                   Long locationId, String cameraType) {
        return authenticationSettingJpaRepository.findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(serviceType,
            locationId, cameraType);
    }

    @Override
    public Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryById(Long id) {
        return authenticationSettingJpaRepository.findAuthenticationSettingSummaryById(id);
    }

    @Override
    public List<AuthenticationWebhookSettingSummary> findAuthenticationWebhooksByAuthenticationSettingId(Long id) {
        return authenticationSettingJpaRepository.findAuthenticationWebhooksByAuthenticationSettingId(id);
    }

    @Override
    public List<AuthenticationWebhookTagSummary> findAuthenticationWebhookTagsByWebhookSettingId(List<Long> webhookSettingIds) {
        return authenticationSettingJpaRepository.findAuthenticationWebhookTagsByWebhookSettingId(webhookSettingIds);
    }
}
