package com.wiredpackage.oauth.api.application.commands.oauth.perform_approval_command;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import com.wiredpackage.auth.infrastructure.services.OAuthService;
import com.wiredpackage.oauth.api.application.events.approved_oauth.ApprovedEvent;
import com.wiredpackage.oauth.api.application.events.rejected_oauth.RejectedOauthEvent;
import com.wiredpackage.oauth.api.application.models.service.ServiceSummary;
import com.wiredpackage.oauth.api.application.queries.service.IServiceQueriesService;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.application.services.PlanService;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate.OAuth2Authentication;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate.OAuth2Grant;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval.OAuth2WaitingApproval;
import com.wiredpackage.oauth.domain.repositories.IOAuth2AuthenticationRepository;
import com.wiredpackage.oauth.domain.repositories.IOAuth2GrantRepository;
import com.wiredpackage.oauth.domain.repositories.IOAuth2WaitingApprovalRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2AuthenticationEntity;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2AuthenticationJpaRepository;
import com.wiredpackage.shared.application.exceptions.BadRequestException;
import com.wiredpackage.shared.application.exceptions.ForbiddenException;
import com.wiredpackage.shared.application.exceptions.NotFoundException;
import com.wiredpackage.shared.application.exceptions.UnauthorizationException;
import com.wiredpackage.shared.shared.constants.Oauth2GrantType;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PerformApprovalCommandHandler implements Command.Handler<PerformApprovalCommand, Void> {
    private final AuthService authService;
    private final IOAuth2WaitingApprovalRepository oAuth2WaitingApprovalRepository;
    private final IOAuth2GrantRepository oAuth2GrantRepository;
    private final IOAuth2AuthenticationRepository oAuth2AuthenticationRepository;
    private final IServiceQueriesService serviceQueriesService;
    private final OAuthService oAuthService;
    private final Pipeline pipeline;
    private final PlanService planService;
    private final OAuth2AuthenticationJpaRepository oAuth2AuthenticationJpaRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Void handle(PerformApprovalCommand command) {
        OAuth2WaitingApproval oAuth2WaitingApproval =
            oAuth2WaitingApprovalRepository.findByItemId(command.getWaitingApprovalItemId()).orElse(null);
        if (oAuth2WaitingApproval == null) {
            throw new NotFoundException("waiting_approval_not_found");
        }

        if (oAuth2WaitingApproval.getApproved() || !oAuth2WaitingApproval.getValid() ||
            authService.isApprovalExpired(oAuth2WaitingApproval.getCreatedAt())) {
            throw new BadRequestException(Map.of("approval", MessageHelper.getMessage("invalid_approval")));
        }

        OAuth2Grant oAuth2Grant = oAuth2GrantRepository.findById(oAuth2WaitingApproval.getOauth2GrantId()).orElseThrow(
            () -> new UnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        ServiceSummary service = serviceQueriesService.findSummaryServiceById(oAuth2Grant.getServiceId()).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage("service_not_found")));
        if (!authService.checkPermissionApproval(service.getType(), oAuth2Grant.getLocationId(), command.getAuthorityId())) {
            throw new ForbiddenException();
        }
        if (Boolean.TRUE.equals(command.getIsApproved())) {
            approve(oAuth2WaitingApproval, oAuth2Grant);
        } else {
            reject(oAuth2WaitingApproval);
        }
        return null;
    }

    private void reject(OAuth2WaitingApproval oAuth2WaitingApproval) {
        oAuth2WaitingApproval.reject();
        oAuth2WaitingApprovalRepository.save(oAuth2WaitingApproval);

        RejectedOauthEvent rejectedOauthEvent = new RejectedOauthEvent(oAuth2WaitingApproval.getItemId());
        pipeline.send(rejectedOauthEvent);
        log.info("----- PerformApprovalCommandHandler.handle() Sent RejectedOauthEvent: {}", rejectedOauthEvent);
    }

    private void approve(OAuth2WaitingApproval oAuth2WaitingApproval, OAuth2Grant oAuth2Grant) {
        oAuth2WaitingApproval.approve();
        oAuth2WaitingApprovalRepository.save(oAuth2WaitingApproval);
        log.info("----- PerformApprovalCommandHandler.handle() Approved OAuth2WaitingApproval: {}", oAuth2WaitingApproval);
        Oauth2GrantType oauth2GrantType = Oauth2GrantType.valueOf(oAuth2Grant.getOauthGrantType());
        if (oauth2GrantType != Oauth2GrantType.STREAM_RECOGNIZANCE
            && oauth2GrantType != Oauth2GrantType.TIGEREYE_BOX_STREAM_RECOGNIZANCE
        ) {
            oAuth2Grant.deactivate();
        }

        oAuth2GrantRepository.save(oAuth2Grant);

        OAuth2Authentication oAuth2Authentication = OAuth2Authentication.builder()
            .identityId(oAuth2WaitingApproval.getIdentityId())
            .authenticationSettingId(oAuth2WaitingApproval.getAuthenticationSettingId())
            .oauth2GrantId(oAuth2WaitingApproval.getOauth2GrantId())
            .expiresAt(oAuthService.getAuthorizationCodeExpiration())
            .authenticationCode(oAuthService.generateAuthorizationCode())
            .authenticationSettingId(oAuth2WaitingApproval.getAuthenticationSettingId())
            .build();
        Optional<OAuth2AuthenticationEntity> existingGrantOauthAuthenticationEntity = oAuth2AuthenticationJpaRepository
            .findFirstByOauth2GrantId(oAuth2WaitingApproval.getOauth2GrantId());
        oAuth2Authentication = oAuth2AuthenticationRepository.saveOrUpdate(oAuth2Authentication, existingGrantOauthAuthenticationEntity);
        planService.checkReachCertificationLimitToSendMail(oAuth2WaitingApproval.getAuthenticationSettingId(), oAuth2Grant.getLocationId());

        ApprovedEvent approvedEvent = new ApprovedEvent(oAuth2Authentication.getAuthenticationCode(),
            oAuth2WaitingApproval.getItemId());
        pipeline.send(approvedEvent);
        log.info("----- PerformApprovalCommandHandler.handle() Sent ApprovedEvent: {}", approvedEvent);
    }
}
