package com.wiredpackage.oauth.api.application.commands.oauth.add_waiting_approval_command;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class AddWaitingApprovalCommand implements Command<String> {
    private Long oauth2GrantId;
    private Long oauth2LogId;
    private Long identityId;
    private Long authenticationSettingId;
    private Long locationId;
    private String service;
    private Long authorityId;
    private List<String> notifyMethods;
    private Long roleId;
}
