package com.wiredpackage.oauth.domain.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_refresh_token_aggregate.OAuth2RefreshToken;
import com.wiredpackage.shared.domain.seed_work.IRepository;

import java.util.Optional;

public interface IOAuth2RefreshTokenRepository extends IRepository<OAuth2RefreshToken> {
    Optional<OAuth2RefreshToken> findByRefreshToken(String refreshToken);
}
