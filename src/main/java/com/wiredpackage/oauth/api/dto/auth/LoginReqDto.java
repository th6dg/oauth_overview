package com.wiredpackage.oauth.api.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReqDto {
    private String loginId;
    private String password;
    private Long accountId;
}
