package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate.OAuth2Authentication;
import com.wiredpackage.oauth.domain.repositories.IOAuth2AuthenticationRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2AuthenticationEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.OAuth2AuthenticationEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2AuthenticationJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class OAuth2AuthenticationRepository implements IOAuth2AuthenticationRepository {
    private final OAuth2AuthenticationJpaRepository oAuth2AuthenticationJpaRepository;
    private final OAuth2AuthenticationEntityMapper mapper;

    @Override
    public OAuth2Authentication save(OAuth2Authentication model) {
        OAuth2AuthenticationEntity oAuth2AuthenticationEntity;
        Optional<OAuth2AuthenticationEntity> existingGrantOauthAuthenticationEntity = oAuth2AuthenticationJpaRepository
            .findFirstByOauth2GrantId(model.getOauth2GrantId());
        if (existingGrantOauthAuthenticationEntity.isEmpty()) {
            oAuth2AuthenticationEntity = mapper.modelToEntity(model);
        } else {
            oAuth2AuthenticationEntity = mapper.updateEntityWithModel(existingGrantOauthAuthenticationEntity.get(), model);
        }
        oAuth2AuthenticationEntity = oAuth2AuthenticationJpaRepository.save(oAuth2AuthenticationEntity);
        return mapper.entityToModel(oAuth2AuthenticationEntity);
    }

    @Override
    public OAuth2Authentication saveOrUpdate(OAuth2Authentication model, Optional<OAuth2AuthenticationEntity> existingGrantOauthAuthenticationEntity) {
        OAuth2AuthenticationEntity oAuth2AuthenticationEntity;
        if (existingGrantOauthAuthenticationEntity.isEmpty()) {
            oAuth2AuthenticationEntity = mapper.modelToEntity(model);
        } else {
            oAuth2AuthenticationEntity = mapper.updateEntityWithModel(existingGrantOauthAuthenticationEntity.get(), model);
        }
        oAuth2AuthenticationEntity = oAuth2AuthenticationJpaRepository.save(oAuth2AuthenticationEntity);
        return mapper.entityToModel(oAuth2AuthenticationEntity);
    }

    @Override
    public void delete(Long id) {
        oAuth2AuthenticationJpaRepository.deleteById(id);
    }

    @Override
    public Optional<OAuth2Authentication> findById(Long id) {
        Optional<OAuth2AuthenticationEntity> oAuth2AuthenticationEntity = oAuth2AuthenticationJpaRepository.findById(id);
        return oAuth2AuthenticationEntity.map(mapper::entityToModel);
    }

    @Override
    public OAuth2Authentication findByAuthenticationCode(String authenticationCode) {
        return mapper.entityToModel(oAuth2AuthenticationJpaRepository.findTopByAuthenticationCode(authenticationCode)
            .orElse(null));
    }

    @Override
    public OAuth2Authentication findByIdentityAndAuthSetting(Long identityId, Long authenticationCode) {
        return mapper.entityToModel(oAuth2AuthenticationJpaRepository
            .findTopByIdentityIdAndAuthenticationSettingIdOrderByCreatedAtDesc(identityId, authenticationCode)
            .orElse(null));
    }

    @Override
    public OAuth2Authentication findByOAuthGrantId(Long oAuthGrantId) {
        return oAuth2AuthenticationJpaRepository.findFirstByOauth2GrantId(oAuthGrantId)
            .map(mapper::entityToModel)
            .orElse(null);
    }
}
