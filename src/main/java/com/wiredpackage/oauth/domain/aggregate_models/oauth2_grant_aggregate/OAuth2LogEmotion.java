package com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate;

import com.wiredpackage.shared.domain.seed_work.Entity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OAuth2LogEmotion extends Entity {
    private Long oauth2LogId;
    private String name;
    private Float value;
}
