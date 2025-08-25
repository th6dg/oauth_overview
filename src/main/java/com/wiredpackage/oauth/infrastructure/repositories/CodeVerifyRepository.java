package com.wiredpackage.oauth.infrastructure.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.code_verify_aggregate.CodeVerify;
import com.wiredpackage.oauth.domain.repositories.ICodeVerifyRepository;
import com.wiredpackage.oauth.infrastructure.entities.CodeVerifyEntity;
import com.wiredpackage.oauth.infrastructure.entity_mappers.CodeVerifyEntityMapper;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.CodeVerifyJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CodeVerifyRepository implements ICodeVerifyRepository {

    private final CodeVerifyJpaRepository codeVerifyJpaRepository;
    private final CodeVerifyEntityMapper mapper;
    @Override
    public CodeVerify save(CodeVerify model) {
        CodeVerifyEntity entity = mapper.modelToEntity(model);
        entity = codeVerifyJpaRepository.save(entity);
        return mapper.entityToModel(entity);
    }

    @Override
    public void delete(Long id) {
        codeVerifyJpaRepository.deleteById(id);
    }

    @Override
    public Optional<CodeVerify> findById(Long id) {
        return codeVerifyJpaRepository.findById(id).map(mapper::entityToModel);
    }

    @Override
    public Optional<CodeVerify> findByCodeVerify(Long identityId, String code, String type, String status,
                                                 LocalDateTime currentTime, String codeChallenge) {
        return codeVerifyJpaRepository.findByCodeVerify(identityId, code, type, status, currentTime, codeChallenge).map(mapper::entityToModel);
    }

    @Override
    public Optional<CodeVerify> findTopByIdentityIdAndCodeChallenge(Long identityId, String codeChallenge) {
        return codeVerifyJpaRepository.findTopByIdentityIdAndCodeChallenge(identityId, codeChallenge).map(mapper::entityToModel);
    }
}
