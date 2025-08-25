package com.wiredpackage.oauth.infrastructure.queries;

import com.wiredpackage.oauth.api.application.models.auth.*;
import com.wiredpackage.oauth.api.application.queries.oauth.IOAuth2QueriesService;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2AuthenticationJpaRepository;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2GrantJpaRepository;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2LogJpaRepository;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2WaitingApprovalJpaRepository;
import com.wiredpackage.shared.shared.utils.HashUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class OAuth2QueriesService implements IOAuth2QueriesService {
    private final OAuth2GrantJpaRepository oAuth2GrantJpaRepository;
    private final OAuth2AuthenticationJpaRepository oAuth2AuthenticationJpaRepository;
    private final OAuth2LogJpaRepository oAuth2LogJpaRepository;
    private final OAuth2WaitingApprovalJpaRepository oAuth2WaitingApprovalJpaRepository;

    @Override
    public boolean existsByCodeChallenge(String codeChallenge) {
        return oAuth2GrantJpaRepository.existsByCodeChallenge(codeChallenge, HashUtils.md5String(codeChallenge), TimeUtils.now()) > 0;
    }

    @Override
    public Optional<OAuth2GrantSummary> findOAuth2GrantSummaryByCodeChallenge(String codeChallenge) {
        return oAuth2GrantJpaRepository.findOAuth2GrantSummaryByCodeChallenge(codeChallenge,
            HashUtils.md5String(codeChallenge), TimeUtils.now());
    }

    @Override
    public Optional<OAuthAuthenticationSummary> findOAuthAuthenticationSummaryByCode(String code) {
        return oAuth2AuthenticationJpaRepository.findOAuthAuthenticationSummaryByHashedCodeAndCodeAndExpiresBefore(
            HashUtils.md5String(code), code, TimeUtils.now());
    }

    @Override
    public Optional<OAuthGrantSummary> findOAuthGrantSummaryById(Long id) {
        return oAuth2GrantJpaRepository.findOAuthGrantSummaryById(id);
    }

    @Override
    public Optional<OAuthLogSummary> getOAuthLogWithCodeChallenge(String codeChallenge) {
        return oAuth2LogJpaRepository.getOAuthLogWithCodeChallenge(HashUtils.md5String(codeChallenge), codeChallenge);
    }

    @Override
    public Long countOAuth2AuthenticationsByAuthenticationSettingIdAndTimeRange(Long authenticationSettingId, LocalDateTime from, LocalDateTime to) {
        return oAuth2AuthenticationJpaRepository.countOAuth2AuthenticationsByAuthenticationSettingIdAndTimeRange(
            authenticationSettingId, from, to);
    }

    @Override
    public Optional<OAuth2WaitingApprovalDetail> findOAuth2WaitingApprovalDetailByWaitingApprovalItemId(String waitingApprovalItemId) {
        return oAuth2WaitingApprovalJpaRepository.findOAuth2WaitingApprovalDetailByWaitingApprovalItemId(waitingApprovalItemId);
    }

    @Override
    public List<Long> findOauth2LogIdsByOauth2GrantIdAndTotalTimeNull(Long oauth2GrantId) {
        return oAuth2LogJpaRepository.findOauth2LogIdsByOauth2GrantIdAndTotalTimeNull(oauth2GrantId);
    }

    @Override
    public List<OauthLogSummaryNotify> findLogsToNotify(Long oauth2GrantId,
                                                        LocalDateTime lastNotificationAt) {
        return oAuth2LogJpaRepository.findLogsToNotify(oauth2GrantId, lastNotificationAt);
    }

    @Override
    public Optional<OAuth2GrantSummary> findOAuth2GrantSummaryServiceTypeByCodeChallenge(String codeChallenge) {
        return oAuth2GrantJpaRepository.findOAuth2GrantSummaryServiceTypeByCodeChallenge(codeChallenge,
            HashUtils.md5String(codeChallenge), TimeUtils.now());
    }
}
