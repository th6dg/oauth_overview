package com.wiredpackage.oauth.api.application.commands.oauth.get_oauth_code_command;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GenerateOAuthCodeCommand implements Command<String> {
    private final Long faceId;
    private final Long identityId;
    private final Long oAuthGrantId;
    private final Long authenticationSettingId;
    private final Long locationId;
}
