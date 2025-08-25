package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval.OAuth2WaitingApproval;
import com.wiredpackage.oauth.domain.repositories.IOAuth2WaitingApprovalRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2WaitingApprovalEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.OAuth2WaitingApprovalEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2WaitingApprovalJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OAuth2WaitingApprovalRepository implements IOAuth2WaitingApprovalRepository {
    private final OAuth2WaitingApprovalJpaRepository oAuth2WaitingApprovalJpaRepository;
    private final OAuth2WaitingApprovalEntityMapper mapper;

    @Override
    public OAuth2WaitingApproval save(OAuth2WaitingApproval model) {
        OAuth2WaitingApprovalEntity entity = mapper.modelToEntity(model);
        entity = oAuth2WaitingApprovalJpaRepository.save(entity);
        return mapper.entityToModel(entity);
    }

    @Override
    public void delete(Long id) {
        oAuth2WaitingApprovalJpaRepository.deleteById(id);
    }

    @Override
    public Optional<OAuth2WaitingApproval> findById(Long id) {
        return oAuth2WaitingApprovalJpaRepository.findById(id).map(mapper::entityToModel);
    }

    @Override
    public Optional<OAuth2WaitingApproval> findByItemId(String itemId) {
        return oAuth2WaitingApprovalJpaRepository.findByItemId(itemId).map(mapper::entityToModel);
    }

    @Override
    public Boolean existsByOauth2GrandId(Long id) {
        return oAuth2WaitingApprovalJpaRepository.existsByOauth2GrantId(id);
    }

    @Override
    public Boolean existByOauth2LogId(Long id) {
        return oAuth2WaitingApprovalJpaRepository.existsByOauth2LogId(id);
    }

    @Override
    public void flush() {
        oAuth2WaitingApprovalJpaRepository.flush();
    }
}
