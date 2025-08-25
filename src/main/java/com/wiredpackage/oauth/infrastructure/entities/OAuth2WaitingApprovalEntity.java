package com.wiredpackage.oauth.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "oauth2_waiting_approval")
public class OAuth2WaitingApprovalEntity extends BaseEntity {
    private String itemId;
    @Column(name = "oauth2_grant_id")
    private Long oauth2GrantId;
    @Column(name = "oauth2_log_id")
    private Long oauth2LogId;
    private Long authenticationSettingId;
    private Long identityId;
    private Boolean approved;
    private Boolean valid;
}
