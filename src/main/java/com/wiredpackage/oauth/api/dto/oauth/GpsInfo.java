package com.wiredpackage.oauth.api.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GpsInfo {
    private String gpsLat;
    private String gpsLong;
}
