package com.wiredpackage.oauth.api.application.models.identity;

public interface IdentitySummary {
    Long getId();

    Long getCompanyId();

    Long getFaceId();

    String getPinCode();

    String getPhoneNumber();

    String getEmail();

    Boolean getIsRegistering();
}
