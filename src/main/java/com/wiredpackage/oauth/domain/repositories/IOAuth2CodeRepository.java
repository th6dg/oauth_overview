package com.wiredpackage.oauth.domain.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_code_aggregate.OAuth2Code;
import com.wiredpackage.shared.domain.seed_work.IRepository;

import java.util.Optional;

public interface IOAuth2CodeRepository extends IRepository<OAuth2Code> {
    Optional<OAuth2Code> findOAuth2CodeByCodeChallenge(String codeChallenge);
}
