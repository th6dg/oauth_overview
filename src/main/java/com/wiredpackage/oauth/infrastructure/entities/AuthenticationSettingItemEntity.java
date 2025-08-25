package com.wiredpackage.oauth.infrastructure.entities;


import jakarta.persistence.*;
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
@Table(name = "authentication_setting_items")
public class AuthenticationSettingItemEntity extends BaseEntity {
    @Column(insertable = false, updatable = false)
    private Long authenticationSettingId;
    private String authenticationSettingField;
    private String value;
    private String hashedValue;

    @ManyToOne
    @JoinColumn(name = "authenticationSettingId", referencedColumnName = "id")
    private AuthenticationSettingEntity authenticationSetting;
}
