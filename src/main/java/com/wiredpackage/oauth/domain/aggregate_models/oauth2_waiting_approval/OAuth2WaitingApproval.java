package com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class OAuth2WaitingApproval extends EntityAggregateRoot {
    private String itemId;
    private Long authenticationSettingId;
    private Long oauth2GrantId;
    private Long oauth2LogId;
    private Long identityId;
    private Boolean approved;
    private Boolean valid;

    public void approve() {
        this.approved = true;
        valid = false;
    }

    public void reject() {
        this.approved = false;
        valid = false;
    }

    public OAuth2WaitingApproval(Long oauth2GrantId, Long oauth2LogId, Long identityId, Long authenticationSettingId) {
        super();
        this.oauth2GrantId = oauth2GrantId;
        this.oauth2LogId = oauth2LogId;
        this.identityId = identityId;
        this.authenticationSettingId = authenticationSettingId;
        this.approved = false;
        this.valid = true;
        this.itemId = UUID.randomUUID().toString();
    }
}
