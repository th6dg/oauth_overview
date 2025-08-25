package com.wiredpackage.oauth.api.dto.authentication;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationGps {
    private Double gpsLat = 0D;
    private Double gpsLong = 0D;
}
