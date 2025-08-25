package com.wiredpackage.oauth.api.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResDto {
    private String accessToken;
    private String refreshToken;
    private Long identityId;
    private String gpsLat;
    private String gpsLong;
}

