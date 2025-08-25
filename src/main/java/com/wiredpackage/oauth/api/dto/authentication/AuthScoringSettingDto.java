package com.wiredpackage.oauth.api.dto.authentication;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthScoringSettingDto {
    private Long id;
    private Long authorityId;
    private List<String> devices;
    private Long retentionTime;
    private boolean brightnessCheck;
    private boolean genderCheck;
    private boolean raceCheck;
    private boolean ageCheck;
    private boolean faceDetectionCheck;
    private boolean onePersonCheck;
    private String liveness;
    private Long authenticationEffectiveDistance;
    private String twoStepVerification;
    private boolean thirdPartyCheck;
    private boolean recognitionImageSavingCheck;
    private boolean isTwoStepActive;
    private boolean autoAdjustFaceSize;
    private String authenticationSizeType;
    private Long authenticationSizeValue;
}
