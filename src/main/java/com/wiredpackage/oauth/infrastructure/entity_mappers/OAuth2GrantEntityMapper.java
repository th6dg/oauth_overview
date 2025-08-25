package com.wiredpackage.oauth.infrastructure.entity_mappers;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate.OAuth2Grant;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2GrantEntity;
import com.wiredpackage.shared.shared.utils.HashUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class OAuth2GrantEntityMapper {

    public OAuth2GrantEntity modelToEntity(OAuth2Grant model) {
        return OAuth2GrantEntity.builder()
            .id(model.id)
            .createdAt(TimeUtils.nullOrNow(model.createdAt))
            .updatedAt(TimeUtils.nullOrNow(model.updatedAt))
            .serviceId(model.getServiceId())
            .locationId(model.getLocationId())
            .clientId(model.getClientId())
            .hashedClientId(HashUtils.md5String(model.getClientId()))
            .codeChallenge(model.getCodeChallenge())
            .hashedCodeChallenge(HashUtils.md5String(model.getCodeChallenge()))
            .expiresAt(model.getExpiresAt())
            .oauthGrantType(model.getOauthGrantType())
            .oauthGrantStatus(model.getOauthGrantStatus())
            .state(model.getState())
            .hashedState(StringUtils.isBlank(model.getState()) ? null : HashUtils.md5String(model.getState()))
            .authenticationObjId(model.getAuthenticationObjId())
            .qrInvitationCodeId(model.getQrInvitationCodeId())
            .startCurrentRecognition(model.getStartCurrentRecognition())
            .aiTime(model.getAiTime())
            .recognizeTime(model.getRecognizeTime())
            .recognizeCount(model.getRecognizeCount())
            .endCurrentRecognition(model.getEndCurrentRecognition())
            .startCurrentVerify(model.getStartCurrentVerify())
            .lastNotificationAt(model.getLastNotificationAt())
            .isSuite(model.getIsSuite())
            .customizeAuthenticationId(model.getCustomizeAuthenticationId())
            .build();
    }

    public OAuth2Grant entityToModel(OAuth2GrantEntity entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }
        return OAuth2Grant.builder()
            .id(entity.getId())
            .createdAt(TimeUtils.nullOrNow(entity.getCreatedAt()))
            .updatedAt(TimeUtils.nullOrNow(entity.getUpdatedAt()))
            .serviceId(entity.getServiceId())
            .locationId(entity.getLocationId())
            .clientId(entity.getClientId())
            .codeChallenge(entity.getCodeChallenge())
            .expiresAt(entity.getExpiresAt())
            .state(entity.getState())
            .oauthGrantType(entity.getOauthGrantType())
            .oauthGrantStatus(entity.getOauthGrantStatus())
            .authenticationObjId(entity.getAuthenticationObjId())
            .qrInvitationCodeId(entity.getQrInvitationCodeId())
            .startCurrentRecognition(entity.getStartCurrentRecognition())
            .aiTime(entity.getAiTime())
            .recognizeTime(entity.getRecognizeTime())
            .recognizeCount(entity.getRecognizeCount())
            .endCurrentRecognition(entity.getEndCurrentRecognition())
            .startCurrentVerify(entity.getStartCurrentVerify())
            .lastNotificationAt(entity.getLastNotificationAt())
            .isSuite(entity.getIsSuite())
            .customizeAuthenticationId(entity.getCustomizeAuthenticationId())
            .build();
    }
}
