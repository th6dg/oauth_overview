package com.wiredpackage.oauth.domain.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate.OAuth2Grant;
import com.wiredpackage.shared.domain.seed_work.IRepository;

import java.util.Optional;

public interface IOAuth2GrantRepository extends IRepository<OAuth2Grant> {
    Optional<OAuth2Grant> findByCodeChallenge(String codeChallenge);

    OAuth2Grant saveAndFlush(OAuth2Grant model);

    Optional<OAuth2Grant> findByOAuth2LogId(Long oAuth2LogId);

    Optional<OAuth2Grant> findByCodeChallengeCanExpire(String codeChallenge);
}
