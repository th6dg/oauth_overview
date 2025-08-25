package com.wiredpackage.oauth.api.application.commands.oauth.perform_self_cancel_approval_command;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wiredpackage.oauth.api.application.queries.authentication.AuthenticationQueries;
import com.wiredpackage.oauth.api.dto.authentication.AuthScoringSettingDto;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval.OAuth2WaitingApproval;
import com.wiredpackage.oauth.domain.repositories.IOAuth2WaitingApprovalRepository;
import com.wiredpackage.oauth.infrastructure.services.MessageService;
import com.wiredpackage.shared.application.exceptions.NotFoundException;
import com.wiredpackage.shared.dto.AddWaitingApprovalMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.wiredpackage.shared.shared.constants.Constants.OAUTH_WAITING_APPROVAL_TOPIC;

@Slf4j
@RequiredArgsConstructor
@Component
public class PerformSelfCancelApprovalCommandHandler implements Command.Handler<PerformSelfCancelApprovalCommand, Void> {
    private final IOAuth2WaitingApprovalRepository oAuth2WaitingApprovalRepository;
    private final AuthenticationQueries authenticationQueries;
    private final MessageService messageService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Void handle(PerformSelfCancelApprovalCommand command) {
        OAuth2WaitingApproval oAuth2WaitingApproval =
            oAuth2WaitingApprovalRepository.findByItemId(command.getWaitingApprovalItemId()).orElse(null);
        if (oAuth2WaitingApproval == null) {
            throw new NotFoundException("waiting_approval_not_found");
        }
        oAuth2WaitingApproval.reject();
        oAuth2WaitingApprovalRepository.save(oAuth2WaitingApproval);
        try {
            AuthScoringSettingDto authScoringSettingDto = authenticationQueries.getAuthScoringByCodeChallenge(command.getCodeChallenge());
            AddWaitingApprovalMsg addWaitingApprovalMsg = new AddWaitingApprovalMsg(command.getService(), command.getLocationId(), authScoringSettingDto.getAuthorityId());
            messageService.publish(OAUTH_WAITING_APPROVAL_TOPIC, addWaitingApprovalMsg);
        } catch (JsonProcessingException e) {
            log.error("----- AddedWaitingOAuthApprovalEventHandler.handle() error when publish to topic: {}", OAUTH_WAITING_APPROVAL_TOPIC, e);
        }

        return null;
    }
}
