package com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Setter
public class OAuth2Authentication extends EntityAggregateRoot {
    private Long identityId;
    private String authenticationCode;
    private LocalDateTime expiresAt;
    private Long oauth2GrantId;
    private Long authenticationSettingId;

    public void deactivate() {
        this.expiresAt = TimeUtils.now();
        this.updatedAt = TimeUtils.now();
    }

    public void updateInfo() {
        this.updatedAt = TimeUtils.now();
    }
}
