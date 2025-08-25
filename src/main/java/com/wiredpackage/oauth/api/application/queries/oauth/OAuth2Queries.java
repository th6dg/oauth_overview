package com.wiredpackage.oauth.api.application.queries.oauth;

import com.wiredpackage.auth.shared.constants.AuthenticationFields;
import com.wiredpackage.auth.shared.constants.OAuthTwoFAType;
import com.wiredpackage.oauth.api.application.models.auth.*;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoring;
import com.wiredpackage.oauth.api.application.models.authentication.AuthenticationSettingSummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentitySummary;
import com.wiredpackage.oauth.api.application.queries.authentication.IAuthenticationSettingQueriesService;
import com.wiredpackage.oauth.api.application.queries.identity.IIdentityQueriesService;
import com.wiredpackage.oauth.api.application.queries.waiting_approval.IWaitingApprovalQueriesService;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.application.services.CodeVerifyService;
import com.wiredpackage.oauth.api.dto.authentication.OAuth2AuthenticationRedisDto;
import com.wiredpackage.oauth.api.dto.oauth.VerifyFaceReqDto;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate.OAuth2Authentication;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_code_aggregate.OAuth2Code;
import com.wiredpackage.oauth.domain.repositories.IOAuth2AuthenticationRepository;
import com.wiredpackage.oauth.domain.repositories.IOAuth2CodeRepository;
import com.wiredpackage.oauth.shared.constants.CodeVerifyType;
import com.wiredpackage.shared.application.exceptions.TaopassNotFoundException;
import com.wiredpackage.shared.application.exceptions.TaopassUnauthorizationException;
import com.wiredpackage.shared.dto.WaitingApprovalResDto;
import com.wiredpackage.shared.infrastructure.services.OAuthAppService;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.wiredpackage.auth.shared.constants.AuthConstants.REDIS_OAUTH2_AUTHENTICATION_PREFIX;
import static com.wiredpackage.shared.shared.utils.TimeUtils.YYYYMMDDHHmmss;

@RequiredArgsConstructor
@Service
@Slf4j
public class OAuth2Queries {
    private final IOAuth2QueriesService oAuth2QueriesService;
    private final IAuthenticationSettingQueriesService authenticationSettingQueriesService;
    private final CodeVerifyService codeVerifyService;
    private final IWaitingApprovalQueriesService waitingApprovalQueriesService;
    private final AuthService authService;
    private final OAuthAppService oauthAppService;
    private final RedisTemplate<String, OAuth2AuthenticationRedisDto> redisTemplateOauth2Authentication;
    private final IOAuth2AuthenticationRepository oAuth2AuthenticationRepository;
    private final IIdentityQueriesService identityQueriesService;
    private final IOAuth2CodeRepository oAuth2CodeRepository;

    @Value("${oauth2.approval.expiration}")
    private Long oauth2ApprovalExpires;

    public boolean existsByCodeChallenge(String codeChallenge) {
        return oAuth2QueriesService.existsByCodeChallenge(codeChallenge);
    }

    public Optional<OAuth2GrantSummary> findOAuth2GrantSummaryByCodeChallenge(String codeChallenge) {
        return oAuth2QueriesService.findOAuth2GrantSummaryByCodeChallenge(codeChallenge);
    }

    public Optional<AuthenticationSettingSummary> findAuthenticationSettingSummaryByClientId(String clientId) {
        return authenticationSettingQueriesService.findAuthenticationSettingSummaryByFieldAndValue(
            AuthenticationFields.CLIENT_ID.name(), clientId);
    }

    public String verifyTwoFactorAuthentication(AuthScoring authScoring, IdentitySummary identity, VerifyFaceReqDto request) {
        return switch (Objects.requireNonNull(OAuthTwoFAType.fromString(authScoring.getTwoStepVerificationType()))) {
            case SMS, EMAIL -> codeVerifyService.verifyCode(request.getVerifyCode(), CodeVerifyType.AUTHENTICATION, identity.getId(), request.getCodeChallenge());
            case PIN -> verifyPin(identity.getPinCode(), request.getVerifyCode());
            default -> throw new TaopassUnauthorizationException(MessageHelper.getMessage("oauth_two_fa_type_invalid"));
        };
    }

    private String verifyPin(String pinCode, String input) {
        if (StringUtils.isBlank(pinCode) || !pinCode.equals(input)) {
            return MessageHelper.getMessage("pin_code_invalid");
        }
        return "";
    }

    public Optional<OAuthAuthenticationSummary> findOAuthAuthenticationSummaryByCode(String code) {
        return oAuth2QueriesService.findOAuthAuthenticationSummaryByCode(code);
    }

    public Optional<OAuthGrantSummary> findOAuthGrantSummaryById(Long id) {
        return oAuth2QueriesService.findOAuthGrantSummaryById(id);
    }

    public OAuthLogSummary getOAuthLogWithCodeChallenge(String codeChallenge) {
        return oAuth2QueriesService.getOAuthLogWithCodeChallenge(codeChallenge).orElseThrow(() ->
            new TaopassNotFoundException(String.format("OauthLog not found with code challenge %s", codeChallenge))
        );
    }

    public Boolean skip2FA(Long faceId, Long authenticationId, Long retentionTime) {
        if (retentionTime == null || retentionTime == 0L) {
            return false;
        }
        // find redis
        String oauth2AuthenticationCacheKey = REDIS_OAUTH2_AUTHENTICATION_PREFIX +
            faceId + '_' + authenticationId;
        OAuth2AuthenticationRedisDto oAuth2Authentication =
            redisTemplateOauth2Authentication.opsForValue().get(oauth2AuthenticationCacheKey);
        // check last time 2fa
        LocalDateTime authenticationTimeLast;
        if (oAuth2Authentication != null && oAuth2Authentication.getAuthenticationTimeLast() != null) {
            authenticationTimeLast = TimeUtils.toLocalDateTime(oAuth2Authentication.getAuthenticationTimeLast(), YYYYMMDDHHmmss);
        } else {
            OAuth2Authentication lastAuthentication = getLastAuthenticationByFaceAndAuthSetting(faceId, authenticationId);
            if (null == lastAuthentication) {
                return false;
            }
            authenticationTimeLast = lastAuthentication.getCreatedAt();
        }
        return authenticationTimeLast.isAfter(TimeUtils.now().minusMinutes(retentionTime));
    }

    private OAuth2Authentication getLastAuthenticationByFaceAndAuthSetting(Long faceId, Long authSettingId) {
        Long identityId = identityQueriesService.findFaceInfoByFaceId(faceId)
            .orElseThrow(() -> {
                log.error("Identity not found with faceId " + faceId);
                return new TaopassNotFoundException("Identity not found with faceId " + faceId);
            }).getIdentityId();
        return oAuth2AuthenticationRepository.findByIdentityAndAuthSetting(identityId, authSettingId);
    }

    public List<WaitingApprovalResDto> getWaitingApprovals(String service, Long locationId, Long authorityId, Long identityId) {
        if (!authService.checkPermissionApproval(service, locationId, authorityId)) {
            return new ArrayList<>();
        }
        IdentityLogin identityLogin = identityQueriesService.findIdentityByIdentityId(identityId)
            .orElseThrow(() -> new TaopassNotFoundException(MessageHelper.getMessage("identity_not_found")));
        if (Boolean.TRUE.equals(identityLogin.getIsRegistering())) {
            return new ArrayList<>();
        }
        return waitingApprovalQueriesService.findAllValidByServiceAndLocationIdAndExpiredBefore(
            service, locationId, TimeUtils.now().minusSeconds(oauth2ApprovalExpires)).stream().map(wa -> {
            String evidence = StringUtils.isBlank(wa.getLogImageObjectKey()) ? "" :
                oauthAppService.getPreSignedLogUrl(wa.getLogImageObjectKey());
            return new WaitingApprovalResDto(wa.getId(), wa.getItemId(), wa.getService(), locationId, wa.getIdentityId(),
                wa.getIdentityFirstName(), wa.getIdentityLastName(), wa.getState(), evidence, wa.getAccessTime());
        }).toList();
    }

    public OAuth2Authentication findOAuth2AuthenticationByCodeChallenge(String codeChallenge) {
        return oAuth2AuthenticationRepository.findByAuthenticationCode(codeChallenge);
    }

    public Optional<OAuth2Code> findOauth2CodeByCodeChallenge(String codeChallenge) {
        return oAuth2CodeRepository.findOAuth2CodeByCodeChallenge(codeChallenge);
    }

    public Optional<OAuth2GrantSummary> findOAuth2GrantSummaryServiceTypeByCodeChallenge(String codeChallenge) {
        return oAuth2QueriesService.findOAuth2GrantSummaryServiceTypeByCodeChallenge(codeChallenge);
    }

    public List<Long> findOauth2LogIdsByOauth2GrantIdAndTotalTimeNull(Long oauth2GrantId) {
        return oAuth2QueriesService.findOauth2LogIdsByOauth2GrantIdAndTotalTimeNull(oauth2GrantId);
    }

    public List<OauthLogSummaryNotify> findLogsToNotify(Long oauth2GrantId,
                                                        LocalDateTime lastNotificationAt) {
        return oAuth2QueriesService.findLogsToNotify(oauth2GrantId, lastNotificationAt);
    }
}
