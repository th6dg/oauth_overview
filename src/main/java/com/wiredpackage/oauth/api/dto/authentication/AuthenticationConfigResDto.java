package com.wiredpackage.oauth.api.dto.authentication;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationConfigResDto {
    // Camera
    private Long cameraId;
    private String cameraName;
    private List<String> numberOfFaceRegistered;
    private List<String> aiSettings;
    private Integer authenticationCount;
    private Integer authenticationInterval;
    private String redirectUrl;
    private String redirectUrlFailed;
    private Long repositoryId;
    private Double gpsLat = 0D;
    private Double gpsLong = 0D;
    private Integer authenticationTimeout;
    private Integer authenticationMinWidth;
    private Integer authenticationMinHeight;
    private Boolean enabledGps;
    private Integer healthCheck;
    private Boolean isLogRecognize;
    private Boolean isNotificationWebhookSendUnregistered;
    private Integer registrationTimeout;

    // AuthScoring
    private List<String> devices;
    private Long retentionTime;
    private boolean brightnessCheck;
    private boolean genderCheck;
    private boolean raceCheck;
    private boolean ageCheck;
    private boolean faceDetectionCheck;
    private boolean onePersonCheck;
    private String liveness;
    private Long authenticationEffectiveDistance;
    private String twoStepVerification;
    private boolean thirdPartyCheck;
    private boolean recognitionImageSavingCheck;
    private boolean isTwoStepActive;
    private boolean autoAdjustFaceSize;
    private Long authorityId;
    private String authenticationSizeType;
    private Long authenticationSizeValue;
    private Long customizeAuthenticationId;
    private Boolean isRegisteredDeviceValid;

    public void setAuthenticationSettings(AuthenticationSettingDto authenticationSettings) {
        cameraId = authenticationSettings.getCameraId();
        cameraName = authenticationSettings.getCameraName();
        numberOfFaceRegistered = authenticationSettings.getNumberOfFaceRegistered();
        aiSettings = authenticationSettings.getAiSettings();
        authenticationCount = authenticationSettings.getAuthenticationCount();
        authenticationInterval = authenticationSettings.getAuthenticationInterval();
        redirectUrl = authenticationSettings.getRedirectUrl();
        redirectUrlFailed = authenticationSettings.getRedirectUrlFailed();
        repositoryId = authenticationSettings.getRepositoryId();
        authenticationTimeout = authenticationSettings.getAuthenticationTimeout();
        authenticationMinWidth = authenticationSettings.getAuthenticationMinWidth();
        authenticationMinHeight = authenticationSettings.getAuthenticationMinHeight();
        enabledGps = authenticationSettings.getEnabledGps();
        healthCheck = authenticationSettings.getHealthCheck();
        isLogRecognize = authenticationSettings.getIsLogRecognize();
        isNotificationWebhookSendUnregistered = authenticationSettings.getIsNotificationWebhookSendUnregistered();
        registrationTimeout = authenticationSettings.getRegistrationTimeout();
    }

    public void setAuthScoringSettings(AuthScoringSettingDto authScoringSettings) {
        if(authScoringSettings == null) return;
        setAuthScoring(authScoringSettings);
        retentionTime = authScoringSettings.getRetentionTime();
        twoStepVerification = authScoringSettings.getTwoStepVerification();
        thirdPartyCheck = authScoringSettings.isThirdPartyCheck();
    }

    public void setAuthScoringSettingsForAuth(AuthScoringSettingDto authScoringSettings) {
        if(authScoringSettings == null) return;
        setAuthScoring(authScoringSettings);
        retentionTime = authScoringSettings.getRetentionTime();
        twoStepVerification = authScoringSettings.getTwoStepVerification();
        thirdPartyCheck = false;
    }

    private void setAuthScoring(AuthScoringSettingDto authScoringSettings) {
        if(authScoringSettings == null) return;
        devices = authScoringSettings.getDevices();
        brightnessCheck = authScoringSettings.isBrightnessCheck();
        genderCheck = authScoringSettings.isGenderCheck();
        raceCheck = authScoringSettings.isRaceCheck();
        ageCheck = authScoringSettings.isAgeCheck();
        faceDetectionCheck = authScoringSettings.isFaceDetectionCheck();
        onePersonCheck = authScoringSettings.isOnePersonCheck();
        liveness = authScoringSettings.getLiveness();
        authenticationEffectiveDistance = authScoringSettings.getAuthenticationEffectiveDistance();
        recognitionImageSavingCheck = authScoringSettings.isRecognitionImageSavingCheck();
        isTwoStepActive = authScoringSettings.isTwoStepActive();
        authorityId = authScoringSettings.getAuthorityId();
        autoAdjustFaceSize = authScoringSettings.isAutoAdjustFaceSize();
        authenticationSizeType = authScoringSettings.getAuthenticationSizeType();
        authenticationSizeValue = authScoringSettings.getAuthenticationSizeValue();
    }
}
