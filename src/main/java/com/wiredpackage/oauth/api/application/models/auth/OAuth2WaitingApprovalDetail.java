package com.wiredpackage.oauth.api.application.models.auth;

import java.time.LocalDateTime;

public interface OAuth2WaitingApprovalDetail {
    Long getId();

    String getItemId();

    Long getOauth2GrantId();

    Long getIdentityId();

    Long getServiceId();

    Boolean getApproved();

    LocalDateTime getCreatedAt();
}
