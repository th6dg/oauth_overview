package com.wiredpackage.oauth.infrastructure.entities;

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
@Table(name = "identities")
public class IdentityEntity extends BaseEntity {
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
    private Long qrInvitationCodeId;
    private String metadata;
}
