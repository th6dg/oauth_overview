package com.wiredpackage.oauth.domain.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate.OAuth2Authentication;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2AuthenticationEntity;
import com.wiredpackage.shared.domain.seed_work.IRepository;

import java.util.Optional;

public interface IOAuth2AuthenticationRepository extends IRepository<OAuth2Authentication> {
    OAuth2Authentication findByAuthenticationCode(String authenticationCode);

    OAuth2Authentication findByIdentityAndAuthSetting(Long identityId, Long authenticationCode);

    OAuth2Authentication findByOAuthGrantId(Long oAuthGrantId);

    OAuth2Authentication saveOrUpdate(OAuth2Authentication oAuth2Authentication, Optional<OAuth2AuthenticationEntity> existingGrantOauthAuthenticationEntity);
}
