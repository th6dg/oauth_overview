package com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate;

import com.wiredpackage.shared.domain.seed_work.EntityAggregateRoot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class StressEmotionLog extends EntityAggregateRoot {
    private Long oauth2LogId;
    private Long scoreHappy;
    private Long scoreFear;
    private Long scoreSad;
    private Long scoreNeutral;
    private Long scoreAngry;
    private Long scoreDisgust;
    private Long scoreSurprised;
}
