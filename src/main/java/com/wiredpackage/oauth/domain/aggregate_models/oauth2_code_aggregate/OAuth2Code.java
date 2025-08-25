package com.wiredpackage.oauth.domain.aggregate_models.oauth2_code_aggregate;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OAuth2Code extends EntityAggregateRoot {
    private String codeChallenge;
    private Long locationId;
    private Long serviceId;
    private String type;
    private String email;
    private String phoneNumber;
    private Boolean isValid;
    private Long systemRegisterIdentityId;

    public void invalidCode() {
        this.isValid = false;
        this.updatedAt = TimeUtils.now();
    }
}
