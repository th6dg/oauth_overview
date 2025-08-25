package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_refresh_token_aggregate.OAuth2RefreshToken;
import com.wiredpackage.oauth.domain.repositories.IOAuth2RefreshTokenRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2RefreshTokenEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.OAuth2RefreshTokenEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2RefreshTokenJpaRepository;
import com.wiredpackage.shared.shared.utils.HashUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class OAuth2RefreshTokenRepository implements IOAuth2RefreshTokenRepository {
    private final OAuth2RefreshTokenJpaRepository oAuth2RefreshTokenJpaRepository;
    private final OAuth2RefreshTokenEntityMapper mapper;

    @Override
    public OAuth2RefreshToken save(OAuth2RefreshToken model) {
        OAuth2RefreshTokenEntity entity = mapper.modelToEntity(model);
        entity = oAuth2RefreshTokenJpaRepository.save(entity);
        return mapper.entityToModel(entity);
    }

    @Override
    public void delete(Long id) {
        oAuth2RefreshTokenJpaRepository.deleteById(id);
    }

    @Override
    public Optional<OAuth2RefreshToken> findById(Long id) {
        Optional<OAuth2RefreshTokenEntity> entity = oAuth2RefreshTokenJpaRepository.findById(id);
        return entity.map(mapper::entityToModel);
    }

    @Override
    public Optional<OAuth2RefreshToken> findByRefreshToken(String refreshToken) {
        return oAuth2RefreshTokenJpaRepository.findByHashedRefreshTokenAndRefreshToken(HashUtils.md5String(refreshToken), refreshToken)
            .map(mapper::entityToModel);
    }
}
