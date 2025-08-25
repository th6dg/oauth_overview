package com.wiredpackage.oauth.api.application.models.waiting_approval;

import java.time.LocalDateTime;

public interface WaitingApprovalBasicInfo {
    Long getId();

    String getItemId();

    String getService();

    String getState();

    Long getIdentityId();

    String getIdentityFirstName();

    String getIdentityLastName();

    String getLogImageObjectKey();

    LocalDateTime getAccessTime();
}
