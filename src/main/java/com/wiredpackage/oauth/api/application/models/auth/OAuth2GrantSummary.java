package com.wiredpackage.oauth.api.application.models.auth;

import java.time.LocalDateTime;

public interface OAuth2GrantSummary {
    Long getId();

    Long getServiceId();

    String getClientId();

    String getCodeChallenge();

    LocalDateTime getExpiresAt();

    String getType();

    Long getAuthenticationObjId();

    Long getLocationId();

    Long getQrInvitationCodeId();

    LocalDateTime getStartCurrentRecognition();

    Long getAiTime();

    Long getRecognizeTime();

    Integer getRecognizeCount();

    LocalDateTime getEndCurrentRecognition();

    LocalDateTime getStartCurrentVerify();

    String getOauthGrantType();

    Boolean getIsSuite();

    String getServiceType();

    Long getCustomizeAuthenticationId();
}
