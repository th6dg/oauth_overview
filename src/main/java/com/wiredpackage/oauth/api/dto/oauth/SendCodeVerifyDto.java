package com.wiredpackage.oauth.api.dto.oauth;

import lombok.Getter;

@Getter
public class SendCodeVerifyDto {
    private String codeChallenge;
    private String typeVerify;
    private Long faceId;
}
