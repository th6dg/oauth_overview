package com.wiredpackage.oauth.infrastructure.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "code_verify")
public class CodeVerifyEntity extends BaseEntity {

    private Long identityId;
    private String code;
    private String type;
    private String status;
    private String codeChallenge;
    private String type2fa;
    private LocalDateTime expiredAt;
}

