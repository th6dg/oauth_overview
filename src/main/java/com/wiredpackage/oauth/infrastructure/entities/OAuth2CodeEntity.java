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
@Table(name = "oauth2_codes")
public class OAuth2CodeEntity extends BaseEntity {
    private String codeChallenge;
    private String hashedCodeChallenge;
    private Long locationId;
    private Long serviceId;
    private String type;
    private String email;
    private String phoneNumber;
    private Boolean isValid;
    private Long systemRegisterIdentityId;
}
