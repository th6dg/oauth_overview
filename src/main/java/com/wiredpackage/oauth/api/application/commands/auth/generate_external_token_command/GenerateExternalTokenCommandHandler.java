package com.wiredpackage.oauth.api.application.commands.auth.generate_external_token_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.oauth.infrastructure.services.TokenService;
import com.wiredpackage.shared.shared.constants.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class GenerateExternalTokenCommandHandler implements Command.Handler<GenerateExternalTokenCommand, String> {
    private final TokenService tokenService;

    @Override
    public String handle(GenerateExternalTokenCommand command) {
        List<String> roles = List.of(RoleEnum.MANAGEMENT_TOKEN.name());
        Long authorityId = 0L;
        return tokenService.signPermanentExternalAccessToken(command.getCompanyId(), command.getManagementTokenId(),
            roles, authorityId);
    }
}
