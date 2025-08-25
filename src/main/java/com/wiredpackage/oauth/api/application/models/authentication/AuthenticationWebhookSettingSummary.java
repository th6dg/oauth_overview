package com.wiredpackage.oauth.api.application.models.authentication;

public interface AuthenticationWebhookSettingSummary {
    Long getId();
    String getUrl();
    Boolean getSendUnregistered();
    Boolean getSendAllRegistered();
    Boolean getSendByTags();
    String getToken();
    String getTagNames();
}
