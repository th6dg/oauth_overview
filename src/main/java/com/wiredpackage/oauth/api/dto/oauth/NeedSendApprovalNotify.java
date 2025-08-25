package com.wiredpackage.oauth.api.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class NeedSendApprovalNotify {
    private Long authorityId;
    private List<String> methods;
}
