package com.wiredpackage.oauth.api.application.queries.auth_scoring;

import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoringDetails;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoringDevice;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoringThirdPartyRecognitionMethod;
import com.wiredpackage.oauth.api.application.models.device_registrations.DeviceRegistrationInfo;

import java.util.List;
import java.util.Optional;

public interface IAuthScoringQueriesService {
    Optional<AuthScoringDetails> findAuthScoringDetailsById(Long id);

    List<AuthScoringDevice> findAuthScoringDevicesByAuthScoringId(Long authScoringId);

    List<AuthScoringThirdPartyRecognitionMethod> findAuthScoringThirdPartyRecognitionMethodsByAuthScoringId(Long authScoringId);

    List<DeviceRegistrationInfo> findDeviceRegistrationsByAuthScoringId(Long authScoringId);
}
