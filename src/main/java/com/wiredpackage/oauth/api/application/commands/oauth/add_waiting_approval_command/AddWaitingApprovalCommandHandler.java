package com.wiredpackage.oauth.api.application.commands.oauth.add_waiting_approval_command;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import com.wiredpackage.oauth.api.application.events.added_waiting_oauth_approval.AddedWaitingOAuthApprovalEvent;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval.OAuth2WaitingApproval;
import com.wiredpackage.oauth.domain.repositories.IOAuth2WaitingApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AddWaitingApprovalCommandHandler implements Command.Handler<AddWaitingApprovalCommand, String> {
    private final IOAuth2WaitingApprovalRepository oAuth2WaitingApprovalRepository;
    private final Pipeline pipeline;

    @Override
    public String handle(AddWaitingApprovalCommand command) {
        try {
            OAuth2WaitingApproval oAuth2WaitingApproval =
                new OAuth2WaitingApproval(command.getOauth2GrantId(), command.getOauth2LogId(), command.getIdentityId(), command.getAuthenticationSettingId());
            if (
                (command.getOauth2LogId() != null && !oAuth2WaitingApprovalRepository.existByOauth2LogId(oAuth2WaitingApproval.getOauth2LogId()))
             || (command.getOauth2GrantId() != null && !oAuth2WaitingApprovalRepository.existsByOauth2GrandId(oAuth2WaitingApproval.getOauth2GrantId()))
            ) {
                oAuth2WaitingApproval = oAuth2WaitingApprovalRepository.save(oAuth2WaitingApproval);
                oAuth2WaitingApprovalRepository.flush();
            }
            log.info("----- AddWaitingApprovalCommandHandler.handle() send AddedOAuth2WaitingApprovalEvent - oAuth2WaitingApprovalId: {}",
                oAuth2WaitingApproval.getId());

            return oAuth2WaitingApproval.getItemId();
        } catch (Exception ex) {
            log.error("----- AddWaitingApprovalCommandHandler.handle() error: {}", ex.getMessage());
            throw ex;
        } finally {
            AddedWaitingOAuthApprovalEvent event = new AddedWaitingOAuthApprovalEvent(command.getService(),
                command.getAuthorityId(), command.getNotifyMethods(), command.getLocationId(), command.getIdentityId());
            pipeline.send(event);
        }
    }
}
