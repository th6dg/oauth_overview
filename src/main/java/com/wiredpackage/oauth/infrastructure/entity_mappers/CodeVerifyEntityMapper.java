package com.wiredpackage.oauth.infrastructure.entity_mappers;

import com.wiredpackage.oauth.domain.aggregate_models.code_verify_aggregate.CodeVerify;
import com.wiredpackage.oauth.infrastructure.entities.CodeVerifyEntity;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.springframework.stereotype.Service;

@Service
public class CodeVerifyEntityMapper {

    public CodeVerifyEntity modelToEntity(CodeVerify model) {
        return CodeVerifyEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .identityId(model.getIdentityId())
            .code(model.getCode())
            .type(model.getType())
            .codeChallenge(model.getCodeChallenge())
            .type2fa(model.getType2fa())
            .status(model.getStatus())
            .expiredAt(model.getExpiredAt())
            .build();
    }

    public CodeVerify entityToModel(CodeVerifyEntity entity) {
        if (entity == null) {
            return null;
        }
        return CodeVerify.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .identityId(entity.getIdentityId())
            .code(entity.getCode())
            .type(entity.getType())
            .codeChallenge(entity.getCodeChallenge())
            .type2fa(entity.getType2fa())
            .status(entity.getStatus())
            .expiredAt(entity.getExpiredAt())
            .build();
    }
}
