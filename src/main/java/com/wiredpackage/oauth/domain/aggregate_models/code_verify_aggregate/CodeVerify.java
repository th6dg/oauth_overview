package com.wiredpackage.oauth.domain.aggregate_models.code_verify_aggregate;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
public class CodeVerify extends EntityAggregateRoot {
    private Long identityId;
    private String code;
    private String type;
    private String status;
    private String codeChallenge;
    private String type2fa;
    private LocalDateTime expiredAt;

    public void upStatus(String status) {
        this.status = status;
        this.updatedAt = TimeUtils.now();
    }
}
