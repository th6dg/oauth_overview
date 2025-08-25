package com.wiredpackage.oauth.api.application.models.auth;


import java.time.LocalDateTime;

public interface OAuthAuthenticationSummary {
    Long getId();

    Long getIdentityId();

    Long getOAuth2GrantId();

    Long getAuthenticationSettingId();

    String getAuthenticationCode();

    LocalDateTime getExpiresAt();
}
