package com.wiredpackage.oauth.api.application.services;

import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.wiredpackage.auth.shared.constants.AuthConstants.REDIS_CAMERA_TIMEOUT_MAX_DAY;
import static com.wiredpackage.auth.shared.constants.AuthConstants.REDIS_CAMERA_TIMEOUT_MIN_DAY;

@Service
public class AuthenticationService {
    public Duration getRedisCameraTimeout() {
        return Duration.ofSeconds(
            (long) (Math.random() * (REDIS_CAMERA_TIMEOUT_MAX_DAY - REDIS_CAMERA_TIMEOUT_MIN_DAY + 1) + REDIS_CAMERA_TIMEOUT_MIN_DAY)
                * 24 * 60 * 60);
    }
}
