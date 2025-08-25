package com.wiredpackage.oauth.api.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogResolvedAuthenticationDto {
    private String code;
    private String codeVerifier;
    private Long resolveTime;
    private Long othersTime;
    private LocalDateTime resolveAt;
}
