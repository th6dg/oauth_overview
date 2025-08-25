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
@Table(name = "oauth2_refresh_tokens")
public class OAuth2RefreshTokenEntity extends BaseEntity {
    private Long identityId;
    private String refreshToken;
    private String hashedRefreshToken;
    private LocalDateTime expiresAt;
}
