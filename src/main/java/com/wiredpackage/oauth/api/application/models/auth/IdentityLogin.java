package com.wiredpackage.oauth.api.application.models.auth;

public interface IdentityLogin {
    Long getId();

    Long getFaceId();

    String getLoginId();

    String getPassword();

    Long getCompanyId();

    Boolean getIsRegistering();
}
