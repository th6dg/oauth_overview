package com.wiredpackage.oauth.api.dto.oauth;

import com.wiredpackage.oauth.api.application.validations.NotExistedCodeChallenge;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
public class RequestOAuthReqDto {
    @NotExistedCodeChallenge
    @NotNull
    @NotEmpty
    @Length(max = 100)
    private String codeChallenge;

    private Long locationId;

    @NotNull
    @NotEmpty
    private String state;

    @NotNull
    @NotEmpty
    private String service;

    private String cameraType;

    @NotNull
    @NotEmpty
    private String authenticationType;

    private Long authObjId;

    private Boolean isSuite;

    private Long customizeAuthenticationId;

    private Boolean isLocation;
}
