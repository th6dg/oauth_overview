package com.wiredpackage.oauth.api.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.commons.nullanalysis.NotNull;

@AllArgsConstructor
@Setter
@Getter
public class PerformApprovalReqDto {
    @NotNull
    private String waitingApprovalItemId;

    @NotNull
    private Boolean isApproved;
}
