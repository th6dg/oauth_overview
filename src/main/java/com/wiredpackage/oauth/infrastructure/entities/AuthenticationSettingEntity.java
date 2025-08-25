package com.wiredpackage.oauth.infrastructure.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "authentication_settings")
public class AuthenticationSettingEntity extends BaseEntity {
    private Long companyId;
    private Long authenticationTypeId;
    private String cameraName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "authenticationSetting", orphanRemoval = true)
    private List<AuthenticationSettingItemEntity> authenticationSettingItems;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "authenticationSetting", orphanRemoval = true)
    private List<AuthenticationAiSettingEntity> authenticationAiSettings;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "authenticationSetting", orphanRemoval = true)
    private List<AuthenticationFaceDirectionEntity> authenticationFaceDirections;
}
