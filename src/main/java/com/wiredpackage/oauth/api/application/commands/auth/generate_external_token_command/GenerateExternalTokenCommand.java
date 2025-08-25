package com.wiredpackage.oauth.api.application.commands.auth.generate_external_token_command;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class GenerateExternalTokenCommand implements Command<String> {
    private Long companyId;
    private Long managementTokenId;
}
