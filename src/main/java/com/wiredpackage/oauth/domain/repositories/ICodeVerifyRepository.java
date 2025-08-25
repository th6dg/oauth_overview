package com.wiredpackage.oauth.domain.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.code_verify_aggregate.CodeVerify;
import com.wiredpackage.shared.domain.seed_work.IRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ICodeVerifyRepository  extends IRepository<CodeVerify> {
    Optional<CodeVerify> findByCodeVerify(Long identityId, String code, String type, String status, LocalDateTime currentTime, String codeChallenge);

    Optional<CodeVerify> findTopByIdentityIdAndCodeChallenge(Long identityId, String codeChallenge);
}
