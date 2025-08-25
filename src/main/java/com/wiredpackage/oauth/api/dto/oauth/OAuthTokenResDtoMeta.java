package com.wiredpackage.oauth.api.dto.oauth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class OAuthTokenResDtoMeta {
    private String type;
    private Long accessLogId;
}
