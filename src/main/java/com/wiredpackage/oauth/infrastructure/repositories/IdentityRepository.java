package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.identity.Identity;
import com.wiredpackage.oauth.domain.repositories.IIdentityRepository;
import com.wiredpackage.oauth.infrastructure.entities.IdentityEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.IdentityEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.IdentityJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class IdentityRepository implements IIdentityRepository {

    private final IdentityJpaRepository identityJpaRepository;
    private final IdentityEntityMapper mapper;

    @Override
    public Identity save(Identity model) {
        IdentityEntity identityEntity = mapper.modelToEntity(model);
        identityEntity = identityJpaRepository.save(identityEntity);
        return mapper.entityToModel(identityEntity);
    }

    @Override
    public void delete(Long id) {
        identityJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Identity> findById(Long id) {
        return identityJpaRepository.findById(id).map(mapper::entityToModel);
    }
}
