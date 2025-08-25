package com.wiredpackage.oauth.api.application.queries.oauth;

import com.wiredpackage.oauth.api.application.models.auth.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IOAuth2QueriesService {
    boolean existsByCodeChallenge(String codeChallenge);

    Optional<OAuth2GrantSummary> findOAuth2GrantSummaryByCodeChallenge(String codeChallenge);

    Optional<OAuthAuthenticationSummary> findOAuthAuthenticationSummaryByCode(String code);

    Optional<OAuthGrantSummary> findOAuthGrantSummaryById(Long id);

    Optional<OAuthLogSummary> getOAuthLogWithCodeChallenge(String codeChallenge);

    Long countOAuth2AuthenticationsByAuthenticationSettingIdAndTimeRange(Long authenticationSettingId, LocalDateTime from, LocalDateTime to);

    Optional<OAuth2WaitingApprovalDetail> findOAuth2WaitingApprovalDetailByWaitingApprovalItemId(String waitingApprovalItemId);

    Optional<OAuth2GrantSummary> findOAuth2GrantSummaryServiceTypeByCodeChallenge(String codeChallenge);

    List<Long> findOauth2LogIdsByOauth2GrantIdAndTotalTimeNull(Long oauth2GrantId);

    List<OauthLogSummaryNotify> findLogsToNotify(Long oauth2GrantId,
                                                 LocalDateTime lastNotificationAt);
}
