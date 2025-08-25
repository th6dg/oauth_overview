package com.wiredpackage.oauth.infrastructure.entity_mappers;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate.OAuth2Authentication;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2AuthenticationEntity;
import com.wiredpackage.shared.shared.utils.HashUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthenticationEntityMapper {
    public OAuth2AuthenticationEntity modelToEntity(OAuth2Authentication model) {
        return OAuth2AuthenticationEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .identityId(model.getIdentityId())
            .authenticationCode(model.getAuthenticationCode())
            .hashedAuthenticationCode(HashUtils.md5String(model.getAuthenticationCode()))
            .expiresAt(model.getExpiresAt())
            .oauth2GrantId(model.getOauth2GrantId())
            .authenticationSettingId(model.getAuthenticationSettingId())
            .build();
    }

    public OAuth2Authentication entityToModel(OAuth2AuthenticationEntity entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }
        return OAuth2Authentication.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .identityId(entity.getIdentityId())
            .authenticationCode(entity.getAuthenticationCode())
            .expiresAt(entity.getExpiresAt())
            .oauth2GrantId(entity.getOauth2GrantId())
            .authenticationSettingId(entity.getAuthenticationSettingId())
            .build();
    }

    public OAuth2AuthenticationEntity updateEntityWithModel(OAuth2AuthenticationEntity entity, OAuth2Authentication model) {
        entity.setIdentityId(model.getIdentityId());
//        entity.setAuthenticationCode(model.getAuthenticationCode());
        entity.setExpiresAt(model.getExpiresAt());
        entity.setAuthenticationSettingId(model.getAuthenticationSettingId());
        entity.setUpdatedAt(TimeUtils.nullOrNow(model.getUpdatedAt()));
        return entity;
    }
}
