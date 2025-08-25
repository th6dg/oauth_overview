package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate.OAuth2Grant;
import com.wiredpackage.oauth.domain.repositories.IOAuth2GrantRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2GrantEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.OAuth2GrantEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2GrantJpaRepository;
import com.wiredpackage.shared.shared.utils.HashUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class OAuth2GrantRepository implements IOAuth2GrantRepository {
    private final OAuth2GrantJpaRepository oAuth2GrantJpaRepository;
    private final OAuth2GrantEntityMapper mapper;

    @Override
    public OAuth2Grant save(OAuth2Grant model) {
        OAuth2GrantEntity entity = mapper.modelToEntity(model);
        entity = oAuth2GrantJpaRepository.save(entity);
        return mapper.entityToModel(entity);
    }

    @Override
    public void delete(Long id) {
        oAuth2GrantJpaRepository.deleteById(id);
    }

    @Override
    public Optional<OAuth2Grant> findById(Long id) {
        Optional<OAuth2GrantEntity> entity = oAuth2GrantJpaRepository.findById(id);
        return entity.map(mapper::entityToModel);
    }

    @Override
    public Optional<OAuth2Grant> findByCodeChallenge(String codeChallenge) {
        Optional<OAuth2GrantEntity> entity = oAuth2GrantJpaRepository.findByHashedCodeChallengeAndCodeChallengeAndExpiresAtAfter(
            HashUtils.md5String(codeChallenge), codeChallenge, TimeUtils.now());
        return entity.map(mapper::entityToModel);
    }

    @Override
    public OAuth2Grant saveAndFlush(OAuth2Grant model) {
        OAuth2GrantEntity entity = mapper.modelToEntity(model);
        entity = oAuth2GrantJpaRepository.save(entity);
        oAuth2GrantJpaRepository.flush();
        return mapper.entityToModel(entity);
    }

    @Override
    public Optional<OAuth2Grant> findByOAuth2LogId(Long oAuth2LogId) {
        Optional<OAuth2GrantEntity> entity = oAuth2GrantJpaRepository.findByOAuth2LogId(oAuth2LogId);
        return entity.map(mapper::entityToModel);
    }

    @Override
    public Optional<OAuth2Grant> findByCodeChallengeCanExpire(String codeChallenge) {
        Optional<OAuth2GrantEntity> entity = oAuth2GrantJpaRepository.findByHashedCodeChallengeAndCodeChallenge(
            HashUtils.md5String(codeChallenge), codeChallenge);
        return entity.map(mapper::entityToModel);
    }
}
