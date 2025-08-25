package com.wiredpackage.oauth.infrastructure.entity_mappers;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_code_aggregate.OAuth2Code;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2CodeEntity;
import com.wiredpackage.shared.shared.utils.HashUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.springframework.stereotype.Service;

@Service
public class OAuth2CodeEntityMapper {
    public OAuth2CodeEntity modelToEntity(OAuth2Code model) {
        return OAuth2CodeEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .codeChallenge(model.getCodeChallenge())
            .hashedCodeChallenge(HashUtils.md5String(model.getCodeChallenge()))
            .locationId(model.getLocationId())
            .serviceId(model.getServiceId())
            .type(model.getType())
            .email(model.getEmail())
            .phoneNumber(model.getPhoneNumber())
            .isValid(model.getIsValid())
            .systemRegisterIdentityId(model.getSystemRegisterIdentityId())
            .build();
    }

    public OAuth2Code entityToModel(OAuth2CodeEntity entity) {
        return OAuth2Code.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .codeChallenge(entity.getCodeChallenge())
            .locationId(entity.getLocationId())
            .serviceId(entity.getServiceId())
            .type(entity.getType())
            .email(entity.getEmail())
            .phoneNumber(entity.getPhoneNumber())
            .isValid(entity.getIsValid())
            .systemRegisterIdentityId(entity.getSystemRegisterIdentityId())
            .build();
    }
}
