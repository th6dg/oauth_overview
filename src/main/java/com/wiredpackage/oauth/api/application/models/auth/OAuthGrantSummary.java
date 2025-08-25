package com.wiredpackage.oauth.api.application.models.auth;

public interface OAuthGrantSummary {
    Long getId();

    String getClientId();

    String getCodeChallenge();
}
