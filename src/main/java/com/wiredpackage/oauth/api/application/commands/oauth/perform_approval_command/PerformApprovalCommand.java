package com.wiredpackage.oauth.api.application.commands.oauth.perform_approval_command;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class PerformApprovalCommand implements Command<Void> {
    private String waitingApprovalItemId;
    private Boolean isApproved;
    private Long identityId;
    private Long authorityId;
}
