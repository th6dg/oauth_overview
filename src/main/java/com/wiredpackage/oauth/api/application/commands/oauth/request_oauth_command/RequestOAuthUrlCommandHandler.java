package com.wiredpackage.oauth.api.application.commands.oauth.request_oauth_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.auth.infrastructure.services.OAuthService;
import com.wiredpackage.oauth.api.application.models.authentication.AuthenticationSettingItem;
import com.wiredpackage.oauth.api.application.models.authentication.AuthenticationSettingSummary;
import com.wiredpackage.oauth.api.application.models.authentication_obj.AuthenticationObjOverview;
import com.wiredpackage.oauth.api.application.models.service.ServiceSummary;
import com.wiredpackage.oauth.api.application.queries.authentication.IAuthenticationSettingQueriesService;
import com.wiredpackage.oauth.api.application.queries.authentication_obj.IAuthenticationObjQueriesService;
import com.wiredpackage.oauth.api.application.queries.service.IServiceQueriesService;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate.OAuth2Grant;
import com.wiredpackage.oauth.domain.repositories.IOAuth2GrantRepository;
import com.wiredpackage.oauth.shared.utils.AuthenticationSettingUtils;
import com.wiredpackage.shared.application.exceptions.TaopassInnerServerErrorException;
import com.wiredpackage.shared.application.exceptions.TaopassNotFoundException;
import com.wiredpackage.shared.shared.constants.CameraType;
import com.wiredpackage.shared.shared.constants.TaopassConstants;
import com.wiredpackage.shared.shared.constants.Oauth2GrantType;
import com.wiredpackage.shared.shared.constants.ServiceType;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wiredpackage.auth.shared.constants.AuthenticationFields.CLIENT_ID;

@Slf4j
@AllArgsConstructor
@Component
public class RequestOAuthUrlCommandHandler implements Command.Handler<RequestOAuthUrlCommand, String> {
    private final IOAuth2GrantRepository oAuth2GrantRepository;
    private final IAuthenticationSettingQueriesService authenticationSettingQueriesService;
    private final IAuthenticationObjQueriesService authenticationObjQueriesService;
    private final IServiceQueriesService serviceQueriesService;
    private final OAuthService oAuthService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handle(RequestOAuthUrlCommand command) {
        AuthenticationSettingSummary authenticationSetting =
            authenticationSettingQueriesService.findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(command.getService(),
                    command.getLocationId(), command.getCameraType())
                .orElseThrow(() -> new TaopassNotFoundException(MessageHelper.getMessage("authentication_setting_not_found")));

        ServiceSummary service = serviceQueriesService.findSummaryServiceByType(command.getService())
            .orElseThrow(() -> new TaopassInnerServerErrorException(MessageHelper.getMessage("service_not_found")));
        boolean isLocation = switch (ServiceType.valueOf(service.getType())) {
            case QR, TAOPASS_AUTH -> true; //Each location has its own authentication obj, but can develop to stamping zone like KINTAI
            case ACCESS_LOG -> false; //Authentication obj related to entrance -> isLocation always false
            case KINTAI ->  command.getIsLocation(); // Base on stamping zone setting
            default -> false;
        };

        if (CameraType.STREAM_CAMERA.name().equals(command.getCameraType())) {
            return generateStreamUrl(service, authenticationSetting.getId(), command.getCodeChallenge(),
                command.getState(), command.getLocationId(), command.getAuthenticationType(), command.getAuthObjId(),
                command.getIsSuite(), command.getCustomizeAuthenticationId(), isLocation);
        }
        return generateOauthUrl(service, authenticationSetting.getId(), command.getCodeChallenge(), command.getState(),
            command.getLocationId(), command.getAuthenticationType(), command.getAuthObjId(),
            command.getIsSuite(), command.getCustomizeAuthenticationId(), isLocation);
    }

    private String generateOauthUrl(ServiceSummary service, Long authenticationSettingId, String codeChallenge, String state,
                                    Long locationId, Oauth2GrantType authenticationType, Long authObjId, Boolean isSuite, Long customizeAuthenticationId, boolean isLocation) {
        AuthenticationSettingItem clientId =
            authenticationSettingQueriesService.findAuthenticationSettingItemByAuthenticationSettingIdAndField(
                authenticationSettingId, CLIENT_ID.name()).orElseThrow(
                () -> new TaopassNotFoundException(MessageHelper.getMessage("authentication_setting_not_found")));

        OAuth2Grant oAuth2Grant = OAuth2Grant.builder()
            .clientId(clientId.getValue())
            .locationId(locationId)
            .codeChallenge(codeChallenge)
            .expiresAt(oAuthService.getCodeChallengeExpiration())
            .serviceId(service.getId())
            .state(state)
            .oauthGrantType(authenticationType.name())
            .authenticationObjId(getAuthObjId(service.getId(), locationId, authObjId, isLocation))
            .isSuite(isSuite)
            .customizeAuthenticationId(customizeAuthenticationId)
            .build();
        oAuth2Grant = oAuth2GrantRepository.save(oAuth2Grant);
        log.info("----- RequestOauthCommandHandler: OAuth2Grant {} was inserted", oAuth2Grant.id);
        return oAuthService.generateOAuthUrl(service.getType(), codeChallenge, state,
            locationId, clientId.getValue(), null, TaopassConstants.OAUTH2_IS_REGISTER_FALSE_VALUE);
    }

    private String generateStreamUrl(ServiceSummary service, Long authenticationSettingId, String codeChallenge, String state,
                                     Long locationId, Oauth2GrantType authenticationType, Long authObjId, Boolean isSuite, Long customizeAuthenticationId, boolean isLocation) {
        OAuth2Grant oAuth2Grant = OAuth2Grant.builder()
            .clientId(AuthenticationSettingUtils.generateStreamClientId(authenticationSettingId))
            .codeChallenge(codeChallenge)
            .expiresAt(oAuthService.getCodeChallengeNoExpirationStreamCamera())
            .serviceId(service.getId())
            .locationId(locationId)
            .state(state)
            .oauthGrantType(authenticationType.name())
            .authenticationObjId(getAuthObjId(service.getId(), locationId, authObjId, isLocation))
            .isSuite(isSuite)
            .customizeAuthenticationId(customizeAuthenticationId)
            .build();
        oAuth2Grant = oAuth2GrantRepository.save(oAuth2Grant);
        log.info("----- RequestOauthCommandHandler: OAuth2Grant {} was inserted", oAuth2Grant.id);
        return oAuthService.generateStreamUrl(service.getType(), authenticationSettingId, codeChallenge,
            state, locationId);
    }

    private Long getAuthObjId(Long serviceId, Long locationId, Long authObjId, boolean isLocation) {
        try {
            List<AuthenticationObjOverview> objOverviews =
                authenticationObjQueriesService.findAllAuthenticationObjOverviewByServiceIdAndLocationId(serviceId,
                    locationId, true, isLocation);
            if (objOverviews == null || objOverviews.isEmpty()) {
                throw new TaopassNotFoundException(MessageHelper.getMessage("authentication_obj_not_found"));
            }
            if (authObjId == null) {
                return objOverviews.get(0).getId();
            }
            return objOverviews
                .stream()
                .filter(obj -> obj.getId().equals(authObjId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("----- RequestOauthCommandHandler: AuthenticationObj {} not found", authObjId);
                    return new TaopassNotFoundException(MessageHelper.getMessage("authentication_obj_not_found"));
                }).getId();
        } catch (Exception e) {
            return null;
        }
    }
}
