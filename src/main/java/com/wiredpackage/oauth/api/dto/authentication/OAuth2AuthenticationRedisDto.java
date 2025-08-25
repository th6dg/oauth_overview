package com.wiredpackage.oauth.api.dto.authentication;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2AuthenticationRedisDto {

    private Long identityId;

    private String authenticationCode;

    private String expiresAt;

    private Long oauth2GrantId;

    private Long authenticationSettingId;

    private String authenticationTimeLast;
}
