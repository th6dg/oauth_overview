package com.wiredpackage.oauth.infrastructure.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "oauth2_grants")
public class OAuth2GrantEntity extends BaseEntity {
    private Long serviceId;
    private Long locationId;
    private String clientId;
    private String hashedClientId;
    private String codeChallenge;
    private String hashedCodeChallenge;
    private String state;
    private String hashedState;
    private LocalDateTime expiresAt;
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
}

