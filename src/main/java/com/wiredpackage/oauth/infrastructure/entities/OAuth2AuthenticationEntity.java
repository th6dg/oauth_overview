package com.wiredpackage.oauth.infrastructure.entities;

import jakarta.persistence.Column;
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
@Table(name = "oauth2_authentications")
public class OAuth2AuthenticationEntity extends BaseEntity {
    private Long identityId;
    private Long authenticationSettingId;
    private String authenticationCode;
    private String hashedAuthenticationCode;
    private LocalDateTime expiresAt;

    @Column(name = "oauth2_grant_id")
    private Long oauth2GrantId;
}
