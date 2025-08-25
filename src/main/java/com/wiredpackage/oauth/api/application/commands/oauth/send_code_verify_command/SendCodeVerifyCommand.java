package com.wiredpackage.oauth.api.application.commands.oauth.send_code_verify_command;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SendCodeVerifyCommand implements Command<Long> {
    private String codeChallenge;
    private Long faceId;
    private String typeVerify;
}
