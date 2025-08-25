package com.wiredpackage.oauth.api.application.commands.auth.login_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.dto.auth.LoginResDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LoginCommand implements Command<LoginResDto> {
    private IdentityLogin identityLogin;
    private Long oauth2AuthenticationId;
    private String gps;
}
