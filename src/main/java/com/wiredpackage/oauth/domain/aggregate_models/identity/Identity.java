package com.wiredpackage.oauth.domain.aggregate_models.identity;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@Getter
@Setter
public class Identity extends EntityAggregateRoot {
    private Long companyId;
    private String loginId;
    private String password;
    private Long faceId;
    private String faceName;
    private String pinCode;
    private Long initialPlanId;
    private Long repoId;
    private Boolean isDeleted;
    private Boolean isRegistering;
    private String metadata;
}
