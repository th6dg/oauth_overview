package com.wiredpackage.oauth.api.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.commons.nullanalysis.NotNull;

@AllArgsConstructor
@Setter
@Getter
public class PerformSelfCancelApprovalReqDto {
    @NotNull
    private String waitingApprovalItemId;

    @NotNull
    private String codeChallenge;

    @NotNull
    private String service;

    @NotNull
    private Long locationId;
}
