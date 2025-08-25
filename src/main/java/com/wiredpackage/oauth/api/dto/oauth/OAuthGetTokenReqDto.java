package com.wiredpackage.oauth.api.dto.oauth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OAuthGetTokenReqDto {
    private String code;
    private String codeVerifier;
    private Boolean managerLoginFace;
}
