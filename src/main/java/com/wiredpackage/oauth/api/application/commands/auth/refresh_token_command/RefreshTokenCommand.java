package com.wiredpackage.oauth.api.application.commands.auth.refresh_token_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.oauth.api.dto.auth.LoginResDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenCommand implements Command<LoginResDto> {
    private String refreshToken;
}
