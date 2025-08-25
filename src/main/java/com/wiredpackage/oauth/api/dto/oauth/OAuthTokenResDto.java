package com.wiredpackage.oauth.api.dto.oauth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class OAuthTokenResDto {
    private String accessToken;
    private String refreshToken;
    private OAuthTokenResDtoMeta meta;
}
