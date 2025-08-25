package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_code_aggregate.OAuth2Code;
import com.wiredpackage.oauth.domain.repositories.IOAuth2CodeRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2CodeEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.OAuth2CodeEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2CodeJpaRepository;
import com.wiredpackage.shared.shared.utils.HashUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class OAuth2CodeRepository implements IOAuth2CodeRepository {
    private final OAuth2CodeJpaRepository oAuth2CodeJpaRepository;
    private final OAuth2CodeEntityMapper mapper;


    @Override
    public OAuth2Code save(OAuth2Code model) {
        OAuth2CodeEntity oAuth2CodeEntity = mapper.modelToEntity(model);
        oAuth2CodeEntity = oAuth2CodeJpaRepository.save(oAuth2CodeEntity);
        return mapper.entityToModel(oAuth2CodeEntity);
    }

    @Override
    public void delete(Long id) {
        oAuth2CodeJpaRepository.deleteById(id);
    }

    @Override
    public Optional<OAuth2Code> findById(Long id) {
        Optional<OAuth2CodeEntity> oAuth2CodeEntity = oAuth2CodeJpaRepository.findById(id);
        return oAuth2CodeEntity.map(mapper::entityToModel);
    }

    @Override
    public Optional<OAuth2Code> findOAuth2CodeByCodeChallenge(String codeChallenge) {
        Optional<OAuth2CodeEntity> oAuth2CodeEntity =
            oAuth2CodeJpaRepository.findByCodeChallenge(HashUtils.md5String(codeChallenge), codeChallenge);
        return oAuth2CodeEntity.map(mapper::entityToModel);
    }
}
