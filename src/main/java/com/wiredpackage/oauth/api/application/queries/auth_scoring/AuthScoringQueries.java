package com.wiredpackage.oauth.api.application.queries.auth_scoring;

import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoring;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoringDetails;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoringDevice;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoringThirdPartyRecognitionMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthScoringQueries {
    private final IAuthScoringQueriesService authScoringQueriesService;

    public Optional<AuthScoring> getAuthScoringById(Long authScoringId) {
        Optional<AuthScoringDetails> authScoring =
            authScoringQueriesService.findAuthScoringDetailsById(authScoringId);
        if (authScoring.isEmpty()) {
            return Optional.empty();
        }

        List<String> authScoringDevices =
            new java.util.ArrayList<>(authScoringQueriesService.findAuthScoringDevicesByAuthScoringId(authScoringId)
                .stream().map(AuthScoringDevice::getType).toList());

        List<String> authScoringThirdPartyRecognitionMethods =
            authScoringQueriesService.findAuthScoringThirdPartyRecognitionMethodsByAuthScoringId(authScoringId)
                .stream().map(AuthScoringThirdPartyRecognitionMethod::getMethod).toList();

        return Optional.of(new AuthScoring(authScoring.get().getId(), authScoring.get().getName(),
            authScoring.get().getBrightness(), authScoring.get().getGenderCheck(), authScoring.get().getRetentionTime(),
            authScoring.get().getRaceCheck(), authScoring.get().getAgeCheck(),
            authScoring.get().getFaceDetection(), authScoring.get().getOnePerson(),
            authScoring.get().getLivenessId(), authScoring.get().getLivenessType(),
            authScoring.get().getAuthenticationEffectiveDistanceId(),
            authScoring.get().getAuthenticationEffectiveDistanceValue(),
            authScoring.get().getAuthorityId(),
            authScoring.get().getTwoStepVerificationId(),
            authScoring.get().getTwoStepVerificationType(), authScoringDevices,
            authScoringThirdPartyRecognitionMethods, authScoring.get().getRecognitionImageSaving(),
            authScoring.get().getIsTwoStepActive(), authScoring.get().getAutoAdjustFaceSize(),
            authScoring.get().getAuthenticationSizeId(),
            authScoring.get().getAuthenticationSizeType(),
            authScoring.get().getAuthenticationSizeValue()));
    }
}
