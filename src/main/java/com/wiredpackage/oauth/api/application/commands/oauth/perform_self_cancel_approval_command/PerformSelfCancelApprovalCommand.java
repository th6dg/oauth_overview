package com.wiredpackage.oauth.api.application.commands.oauth.perform_self_cancel_approval_command;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class PerformSelfCancelApprovalCommand implements Command<Void> {
    private String waitingApprovalItemId;
    private String service;
    private Long locationId;
    private String codeChallenge;
}
