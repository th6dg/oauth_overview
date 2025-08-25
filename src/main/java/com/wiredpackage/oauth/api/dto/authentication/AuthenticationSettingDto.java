package com.wiredpackage.oauth.api.dto.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationSettingDto {
    private Long cameraId;
    private String cameraName;
    private List<String> numberOfFaceRegistered;
    private List<String> aiSettings;
    private Integer authenticationCount;
    private Integer authenticationInterval;
    private String redirectUrl;
    private String redirectUrlFailed;
    private Long repositoryId;
    private Integer authenticationTimeout;
    private Integer authenticationMinWidth;
    private Integer authenticationMinHeight;
    private Boolean enabledGps;
    private Integer healthCheck;
    private Boolean isLogRecognize;
    private Boolean isNotificationWebhookSendUnregistered;
    private Integer registrationTimeout;
}
