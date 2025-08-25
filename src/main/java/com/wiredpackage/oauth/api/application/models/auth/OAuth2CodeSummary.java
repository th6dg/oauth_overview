package com.wiredpackage.oauth.api.application.models.auth;

public interface OAuth2CodeSummary {
    Long getLocationId();

    Long getServiceId();

    String getType();

    String getEmail();

    String getCodeChallenge();

    Boolean getIsValid();
}
