package com.wiredpackage.oauth.api.application.commands.oauth.request_oauth_command;


import an.awesome.pipelinr.Command;
import com.wiredpackage.shared.shared.constants.Oauth2GrantType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RequestOAuthUrlCommand implements Command<String> {
    private String codeChallenge;
    private String service;
    private Long locationId;
    private String state;
    private String cameraType;
    private Oauth2GrantType authenticationType;
    private Long authObjId;
    private Boolean isSuite;
    private Long customizeAuthenticationId;
    private Boolean isLocation;
}
