package com.wiredpackage.oauth.api.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class NotifyUnknownWebhookReqDto {
    private String age;
    private String gender;
    private List<Integer> bbox;
    private NotifyEmotionDto emotion;
}
