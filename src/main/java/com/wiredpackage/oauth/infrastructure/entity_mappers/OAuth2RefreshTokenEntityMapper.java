package com.wiredpackage.oauth.infrastructure.entity_mappers;


import com.wiredpackage.oauth.domain.aggregate_models.oauth2_refresh_token_aggregate.OAuth2RefreshToken;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2RefreshTokenEntity;
import com.wiredpackage.shared.shared.utils.HashUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
public class OAuth2RefreshTokenEntityMapper {
    public OAuth2RefreshTokenEntity modelToEntity(OAuth2RefreshToken model) {
        return OAuth2RefreshTokenEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .identityId(model.getIdentityId())
            .refreshToken(model.getRefreshToken())
            .hashedRefreshToken(HashUtils.md5String(model.getRefreshToken()))
            .expiresAt(model.getExpiresAt())
            .build();
    }

    public OAuth2RefreshToken entityToModel(OAuth2RefreshTokenEntity entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }
        return OAuth2RefreshToken.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .identityId(entity.getIdentityId())
            .refreshToken(entity.getRefreshToken())
            .expiresAt(entity.getExpiresAt())
            .build();
    }
}

