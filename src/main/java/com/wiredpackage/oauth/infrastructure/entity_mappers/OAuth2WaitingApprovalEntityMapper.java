package com.wiredpackage.oauth.infrastructure.entity_mappers;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval.OAuth2WaitingApproval;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2WaitingApprovalEntity;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.springframework.stereotype.Service;

@Service
public class OAuth2WaitingApprovalEntityMapper {
    public OAuth2WaitingApprovalEntity modelToEntity(OAuth2WaitingApproval model) {
        return OAuth2WaitingApprovalEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .itemId(model.getItemId())
            .oauth2GrantId(model.getOauth2GrantId())
            .oauth2LogId(model.getOauth2LogId())
            .authenticationSettingId(model.getAuthenticationSettingId())
            .identityId(model.getIdentityId())
            .approved(model.getApproved())
            .valid(model.getValid())
            .build();
    }

    public OAuth2WaitingApproval entityToModel(OAuth2WaitingApprovalEntity entity) {
        if (entity == null) {
            return null;
        }
        return OAuth2WaitingApproval.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .itemId(entity.getItemId())
            .oauth2GrantId(entity.getOauth2GrantId())
            .oauth2LogId(entity.getOauth2LogId())
            .authenticationSettingId(entity.getAuthenticationSettingId())
            .identityId(entity.getIdentityId())
            .approved(entity.getApproved())
            .valid(entity.getValid())
            .build();
    }
}
