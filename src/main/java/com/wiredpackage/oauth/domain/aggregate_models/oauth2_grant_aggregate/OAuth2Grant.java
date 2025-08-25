package com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Setter
public class OAuth2Grant extends EntityAggregateRoot {
    private Long serviceId;
    private Long locationId;
    private String clientId;
    private String codeChallenge;
    private LocalDateTime expiresAt;
    private String state;

    private String oauthGrantType;
    private String oauthGrantStatus;
    private Long authenticationObjId;
    private Long qrInvitationCodeId;

    private LocalDateTime startCurrentRecognition;
    private Long aiTime;
    private Long recognizeTime;
    private Integer recognizeCount;
    private LocalDateTime endCurrentRecognition;
    private LocalDateTime startCurrentVerify;

    private LocalDateTime lastNotificationAt;

    private Boolean isSuite;
    private Long customizeAuthenticationId;

    public void deactivate() {
        this.expiresAt = TimeUtils.now();
        this.updatedAt = TimeUtils.now();
    }

    public void updateInfo() {
        this.updatedAt = TimeUtils.now();
    }

    public void updateRecognitionAttempt(Long aiTime, Long recognizeTime, LocalDateTime startCurrentRecognize,
                                         LocalDateTime endCurrentRecognize) {
        if (this.recognizeCount == null || this.recognizeCount == 0) {
            this.startCurrentRecognition = startCurrentRecognize;
            this.aiTime = 0L;
            this.recognizeTime = 0L;
            this.recognizeCount = 0;
        }
        this.aiTime += aiTime;
        this.recognizeTime += recognizeTime;
        this.recognizeCount += 1;
        this.endCurrentRecognition = endCurrentRecognize;
        this.updatedAt = TimeUtils.now();
    }

    public void updateStartVerifyTime(LocalDateTime startVerifyTime) {
        if (this.startCurrentVerify == null) {
            this.startCurrentVerify = startVerifyTime;
            this.updatedAt = TimeUtils.now();
        }
    }

    public void resetVerifyTime() {
        this.startCurrentRecognition = null;
        this.aiTime = 0L;
        this.recognizeTime = 0L;
        this.recognizeCount = 0;
        this.endCurrentRecognition = null;
        this.startCurrentVerify = null;
    }
}
