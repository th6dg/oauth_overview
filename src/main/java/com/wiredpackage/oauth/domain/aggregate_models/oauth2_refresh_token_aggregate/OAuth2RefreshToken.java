package com.wiredpackage.oauth.domain.aggregate_models.oauth2_refresh_token_aggregate;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
public class OAuth2RefreshToken extends EntityAggregateRoot {
    private Long identityId;
    private String refreshToken;
    private LocalDateTime expiresAt;
}
