package com.wiredpackage.oauth.infrastructure.entity_mappers;

import com.wiredpackage.oauth.domain.aggregate_models.identity.Identity;
import com.wiredpackage.oauth.infrastructure.entities.IdentityEntity;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class IdentityEntityMapper {

    public IdentityEntity modelToEntity(Identity model) {
        return IdentityEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .companyId(model.getCompanyId())
            .loginId(model.getLoginId())
            .password(model.getPassword())
            .faceId(model.getFaceId())
            .faceName(model.getFaceName())
            .pinCode(StringUtils.isBlank(model.getPinCode()) ? null : model.getPinCode())
            .initialPlanId(model.getInitialPlanId())
            .repoId(model.getRepoId())
            .isDeleted(model.getIsDeleted())
            .isRegistering(model.getIsRegistering() != null && model.getIsRegistering())
            .metadata(model.getMetadata())
            .build();
    }

    public Identity entityToModel(IdentityEntity entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }
        return Identity.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .companyId(entity.getCompanyId())
            .loginId(entity.getLoginId())
            .password(entity.getPassword())
            .faceId(entity.getFaceId())
            .faceName(entity.getFaceName())
            .pinCode(entity.getPinCode())
            .initialPlanId(entity.getInitialPlanId())
            .repoId(entity.getRepoId())
            .isDeleted(entity.getIsDeleted())
            .isRegistering(entity.getIsRegistering())
            .metadata(entity.getMetadata())
            .build();
    }
}
