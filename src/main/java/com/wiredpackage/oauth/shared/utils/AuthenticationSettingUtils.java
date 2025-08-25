package com.wiredpackage.oauth.shared.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import static com.wiredpackage.shared.shared.constants.Constants.CLIENT_ID_STREAM_CAMERA;

public class AuthenticationSettingUtils {

    public static String AI_SETTINGS_NAME = "NAME";
    public static String AI_SETTINGS_EMOTION = "EMOTION";
    public static String AI_SETTINGS_AGE = "AGE";
    public static String AI_SETTINGS_GENDER = "GENDER";

    public static boolean isStreamClientId(String clientId) {
        return StringUtils.contains(clientId, CLIENT_ID_STREAM_CAMERA);
    }

    public static String generateStreamClientId(Long authenticationSettingId) {
        return StringUtils.join(CLIENT_ID_STREAM_CAMERA, authenticationSettingId);
    }

    public static Long getCameraIdFromClientIdStream(String clientId) {
        String authenticationSettingId = StringUtils.remove(clientId, CLIENT_ID_STREAM_CAMERA);
        return NumberUtils.toLong(authenticationSettingId);
    }
}
