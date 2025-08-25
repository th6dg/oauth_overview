package com.wiredpackage.oauth.api.application.queries.authentication;

import com.wiredpackage.auth.shared.constants.AuthenticationFields;
import com.wiredpackage.auth.shared.constants.AuthenticationObjItemField;
import com.wiredpackage.oauth.api.application.models.ai_setting.AiSetting;
import com.wiredpackage.oauth.api.application.models.auth.OAuth2GrantSummary;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoring;
import com.wiredpackage.oauth.api.application.models.authentication.*;
import com.wiredpackage.oauth.api.application.models.plan.PlanSummary;
import com.wiredpackage.oauth.api.application.models.schedule.Schedule;
import com.wiredpackage.oauth.api.application.models.service.ServiceSummary;
import com.wiredpackage.oauth.api.application.queries.ai_setting.IAiSettingQueriesService;
import com.wiredpackage.oauth.api.application.queries.auth_scoring.AuthScoringQueries;
import com.wiredpackage.oauth.api.application.queries.auth_scoring.IAuthScoringQueriesService;
import com.wiredpackage.oauth.api.application.queries.authentication_obj.AuthenticationObjQueries;
import com.wiredpackage.oauth.api.application.queries.company.CompanyQueries;
import com.wiredpackage.oauth.api.application.queries.oauth.OAuth2Queries;
import com.wiredpackage.oauth.api.application.queries.plan.IPlanQueriesService;
import com.wiredpackage.oauth.api.application.queries.schedule.ScheduleQueries;
import com.wiredpackage.oauth.api.application.queries.service.IServiceQueriesService;
import com.wiredpackage.oauth.api.application.services.AuthenticationService;
import com.wiredpackage.oauth.api.application.services.PlanService;
import com.wiredpackage.oauth.api.application.services.ScheduleService;
import com.wiredpackage.oauth.api.dto.authentication.AuthScoringSettingDto;
import com.wiredpackage.oauth.api.dto.authentication.AuthenticationConfigResDto;
import com.wiredpackage.oauth.api.dto.authentication.AuthenticationGps;
import com.wiredpackage.oauth.api.dto.authentication.AuthenticationSettingDto;
import com.wiredpackage.shared.application.dto.authentication_obj.AuthenticationObjDetails;
import com.wiredpackage.shared.application.dto.suite_customize_authentication.SuiteCustomizeAuthenticationDto;
import com.wiredpackage.shared.application.exceptions.InnerServerErrorException;
import com.wiredpackage.shared.application.exceptions.NotFoundException;
import com.wiredpackage.shared.application.exceptions.UnauthorizationException;
import com.wiredpackage.shared.dto.device_registrations.DeviceRegistrationStatus;
import com.wiredpackage.shared.infrastructure.sevice_clients.accounts.AccountsServiceClient;
import com.wiredpackage.shared.shared.constants.CameraType;
import com.wiredpackage.shared.shared.constants.Oauth2GrantType;
import com.wiredpackage.shared.shared.constants.ServiceType;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.wiredpackage.auth.shared.constants.AuthConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationQueries {
    private final IAuthenticationSettingQueriesService authenticationSettingQueriesService;
    private final IPlanQueriesService planQueriesService;
    private final ScheduleQueries scheduleQueries;
    private final AuthScoringQueries authScoringQueries;
    private final IServiceQueriesService serviceQueriesService;
    private final AuthenticationObjQueries authenticationObjQueries;
    private final CompanyQueries companyQueries;
    private final IAiSettingQueriesService aiSettingQueriesService;
    private final IAuthScoringQueriesService authScoringQueriesService;

    private final OAuth2Queries oAuth2Queries;
    private final RedisTemplate<String, AuthenticationSettingDto> redisTemplateAuthentication;
    private final RedisTemplate<String, Schedule> redisTemplateSchedule;
    private final RedisTemplate<String, AuthScoring> redisTemplateAuthScoring;
    private final AuthenticationService authenticationService;
    private final ScheduleService scheduleService;
    private final PlanService planService;
    private final AccountsServiceClient accountsServiceClient;

    @Value("${services.suite.redirect_url.kintai}")
    private String suiteRedirectUrlKintai;
    @Value("${services.suite.redirect_url.access_log}")
    private String suiteRedirectUrlAccessLog;
    @Value("${services.suite.redirect_url.qr}")
    private String suiteRedirectUrlQr;
    @Value("${services.suite.redirect_url.ticket}")
    private String suiteRedirectUrlTicket;

    public AuthenticationConfigResDto getAuthenticationConfig(String clientId, Long locationId, String codeChallenge, String deviceIdentity) {
        OAuth2GrantSummary oAuth2Grant =
            oAuth2Queries.findOAuth2GrantSummaryServiceTypeByCodeChallenge(codeChallenge).orElseThrow(
                () -> new UnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        AuthenticationConfigResDto authenticationConfigResDto = new AuthenticationConfigResDto();

        AuthenticationSettingDto authenticationSettings = getCameraConfig(clientId, locationId);
        authenticationConfigResDto.setAuthenticationSettings(authenticationSettings);

        if (Boolean.TRUE.equals(oAuth2Grant.getIsSuite())) {
            String redirectUrl = switch (ServiceType.valueOf(oAuth2Grant.getServiceType())) {
                case KINTAI -> suiteRedirectUrlKintai;
                case ACCESS_LOG -> suiteRedirectUrlAccessLog;
                case QR -> suiteRedirectUrlQr;
                case TICKET -> suiteRedirectUrlTicket;
                default -> throw new InnerServerErrorException(MessageHelper.getMessage("service_not_found"));
            };

            // Handle default authentication
            SuiteCustomizeAuthenticationDto suiteDefaultAuthenticationDto =
                accountsServiceClient.getDefaultAuthentication(locationId, oAuth2Grant.getServiceId());
            if (suiteDefaultAuthenticationDto != null) {
                applyAuthenticationDtoToResponse(suiteDefaultAuthenticationDto, authenticationConfigResDto);
                authenticationConfigResDto.setAuthenticationTimeout(suiteDefaultAuthenticationDto.getAuthenticationTimeout());
            }

            // Handle customize authentication if available
            if (oAuth2Grant.getCustomizeAuthenticationId() != null) {
                SuiteCustomizeAuthenticationDto suiteCustomizeAuthenticationDto =
                    accountsServiceClient.getCustomizeAuthentication(oAuth2Grant.getCustomizeAuthenticationId());
                if (suiteCustomizeAuthenticationDto == null) {
                    throw new NotFoundException(MessageHelper.getMessage("suite_customize_authentication_not_found"));
                }
                applyAuthenticationDtoToResponse(suiteCustomizeAuthenticationDto, authenticationConfigResDto);
                authenticationConfigResDto.setCustomizeAuthenticationId(oAuth2Grant.getCustomizeAuthenticationId());
            }

            authenticationConfigResDto.setRedirectUrl(redirectUrl);
        }
        if (Boolean.TRUE.equals(authenticationConfigResDto.getIsNotificationWebhookSendUnregistered())) {
            authenticationConfigResDto.setIsNotificationWebhookSendUnregistered(
                getStatusUnknownLog(locationId));
        }
        if (oAuth2Grant.getType().equals(Oauth2GrantType.REGISTRATION.name()) && oAuth2Grant.getServiceType().equals(ServiceType.TICKET.name())) {
            authenticationConfigResDto.setRedirectUrl(planService.generateTicketStaffLoginUrl(oAuth2Grant.getLocationId(), oAuth2Grant.getServiceId()));
        }

        ServiceSummary service = serviceQueriesService.findSummaryServiceById(oAuth2Grant.getServiceId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(
                "service_not_found")));
        if (oAuth2Grant.getAuthenticationObjId() != null) {
            if (service.getType().equals(ServiceType.AUTH.name())) {
                // Save authenticationObjId = scheduleId only for AUTH
                Long scheduleId = oAuth2Grant.getAuthenticationObjId();
                AuthScoringSettingDto authScoringSettings = getScheduleValues(Set.of(scheduleId));
                authenticationConfigResDto.setAuthScoringSettingsForAuth(authScoringSettings);
                return authenticationConfigResDto;
            }
            List<AuthenticationObjDetails> authenticationObjDetails = authenticationObjQueries.findAllItemsByObjId(
                oAuth2Grant.getAuthenticationObjId());
            Map<String, List<AuthenticationObjDetails>> authenticationObjItems =
                authenticationObjDetails.stream().collect(Collectors.groupingBy(AuthenticationObjDetails::getType));
            if (authenticationObjItems.isEmpty() ||
                !authenticationObjItems.containsKey(AuthenticationObjItemField.SCHEDULE_ID.name())) {
                return authenticationConfigResDto;
            }
            Boolean restrictByGps =
                Boolean.valueOf(authenticationObjItems.get(AuthenticationObjItemField.RESTRICT_BY_GPS.name())
                    .get(0)
                    .getValue());
            if (Boolean.TRUE.equals(restrictByGps)) {
                Double latitude = Double.valueOf(authenticationObjItems.get(AuthenticationObjItemField.LATITUDE.name())
                    .get(0)
                    .getValue());
                Double longitude =
                    Double.valueOf(authenticationObjItems.get(AuthenticationObjItemField.LONGITUDE.name())
                        .get(0)
                        .getValue());
                authenticationConfigResDto.setGpsLat(latitude);
                authenticationConfigResDto.setGpsLong(longitude);
            }
            Set<Long> scheduleIds =
                authenticationObjItems.get(AuthenticationObjItemField.SCHEDULE_ID.name()).stream().map(
                    AuthenticationObjDetails::getValue).map(Long::parseLong).collect(Collectors.toSet());
            AuthScoringSettingDto authScoringSettings = getScheduleValues(scheduleIds);
            authenticationConfigResDto.setAuthScoringSettings(authScoringSettings);
            authenticationConfigResDto.setIsRegisteredDeviceValid(
                isRegisteredDeviceValid(authScoringSettings.getId(), deviceIdentity)
            );
        }
        authenticationConfigResDto.setGpsLat(authenticationConfigResDto.getGpsLat() == null ?
            0D : authenticationConfigResDto.getGpsLat());
        authenticationConfigResDto.setGpsLong(authenticationConfigResDto.getGpsLong() == null ?
            0D : authenticationConfigResDto.getGpsLong());

        return authenticationConfigResDto;
    }

    //todo check here
    private void cacheAuthenticationSetting(String clientId, AuthenticationSettingDto settings) {
        String authenticationCacheKey = getAuthenticationCacheKey(clientId);
        redisTemplateAuthentication.opsForValue().set(authenticationCacheKey,
            settings, authenticationService.getRedisCameraTimeout());
        log.info("----- Save new oauth cache by key {}", authenticationCacheKey);
    }

    public AuthenticationSettingDto getCameraConfig(String clientId, Long locationId) {
        String authenticationCacheKey = getAuthenticationCacheKey(clientId);
        AuthenticationSettingDto authenticationSettings = redisTemplateAuthentication.opsForValue().get(authenticationCacheKey);

        if (authenticationSettings == null
            || ObjectUtils.isEmpty(authenticationSettings.getCameraName())
        ) {
            AuthenticationSettingSummary camera =
                authenticationSettingQueriesService.findAuthenticationSettingSummaryByFieldAndValue(
                    AuthenticationFields.CLIENT_ID.name(), clientId).orElseThrow(
                    () -> new NotFoundException(MessageHelper.getMessage("authentication_setting_not_found")));

            PlanSummary plan = planQueriesService.findPlanSummaryByAuthenticationCameraIdAndLocationId(
                camera.getId(), locationId, TimeUtils.currentDate()).orElseThrow(
                () -> new NotFoundException(MessageHelper.getMessage("plan_not_found")));

            List<String> aiSettings =
                authenticationSettingQueriesService.findAuthenticationAiSettingsByAuthenticationSettingId(
                    camera.getId()).stream().map(AuthenticationAiSetting::getType).toList();
            List<String> faceDirections =
                authenticationSettingQueriesService.findAuthenticationFaceDirectionsByAuthenticationSettingId(
                    camera.getId()).stream().map(AuthenticationFaceDirection::getType).toList();

            Map<String, AuthenticationSettingItem> authenticationSettingItems =
                authenticationSettingQueriesService.findAuthenticationSettingItemsByAuthenticationSettingId(
                        camera.getId()).stream()
                    .collect(Collectors.toMap(AuthenticationSettingItem::getAuthenticationSettingField, item -> item));

            Boolean isNotificationWebhookSendUnregistered =
                authenticationSettingQueriesService.findAuthenticationWebhooksByAuthenticationSettingId(camera.getId())
                    .stream()
                    .anyMatch(AuthenticationWebhookSettingSummary::getSendUnregistered);

            authenticationSettings = AuthenticationSettingDto.builder()
                .aiSettings(aiSettings)
                .numberOfFaceRegistered(faceDirections)
                .authenticationCount(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_COUNT.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_COUNT.name())
                                .getValue()))
                .authenticationInterval(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_INTERVAL.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_INTERVAL.name())
                                .getValue()))
                .redirectUrl(
                    authenticationSettingItems.get(AuthenticationFields.REDIRECT_URL.name()) == null ? null :
                        authenticationSettingItems.get(AuthenticationFields.REDIRECT_URL.name()).getValue())
                .redirectUrlFailed(authenticationSettingItems.get(AuthenticationFields.REDIRECT_URL_FAILED.name()) == null ? null :
                    authenticationSettingItems.get(AuthenticationFields.REDIRECT_URL_FAILED.name()).getValue())
                .authenticationTimeout(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_TIMEOUT.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_TIMEOUT.name())
                                .getValue()))
                .authenticationMinWidth(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_WIDTH.name()) == null ?
                        null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_WIDTH.name())
                                .getValue()))
                .authenticationMinHeight(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_HEIGHT.name()) == null ?
                        null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_HEIGHT.name())
                                .getValue()))
                .isLogRecognize(
                    authenticationSettingItems.get(AuthenticationFields.IS_LOG_RECOGNIZE.name()) != null &&
                        Boolean.parseBoolean(
                            authenticationSettingItems.get(AuthenticationFields.IS_LOG_RECOGNIZE.name())
                                .getValue()))
                .enabledGps(authenticationSettingItems.get(AuthenticationFields.ENABLED_GPS.name()) == null
                    || Boolean.parseBoolean(authenticationSettingItems.get(AuthenticationFields.ENABLED_GPS.name()).getValue()))
                .healthCheck(
                    authenticationSettingItems.get(AuthenticationFields.HEALTH_CHECK.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.HEALTH_CHECK.name())
                                .getValue()))
                .repositoryId(plan.getAiRepoId())
                .cameraId(camera.getId())
                .cameraName(camera.getCameraName())
                .isNotificationWebhookSendUnregistered(isNotificationWebhookSendUnregistered)
                .registrationTimeout(
                    authenticationSettingItems.get(AuthenticationFields.REGISTRATION_TIMEOUT.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.REGISTRATION_TIMEOUT.name())
                                .getValue()))
                .build();
            cacheAuthenticationSetting(clientId, authenticationSettings);
        }
        return authenticationSettings;
    }

    private AuthScoringSettingDto getScheduleValues(Set<Long> scheduleIds) {
        AuthScoringSettingDto res = new AuthScoringSettingDto();
        if (scheduleIds.isEmpty()) {
            return res;
        }
        Optional<Schedule> scheduleOpt = getSchedule(scheduleIds);
        if (scheduleOpt.isEmpty()) {
            return res;
        }
        AuthScoring authScoring = getAuthScoring(scheduleOpt.get().getAuthScoringId());
        if (authScoring == null) {
            return res;
        }
        res.setId(authScoring.getId());
        res.setAuthorityId(authScoring.getAuthorityId());
        res.setDevices(authScoring.getDevices());
        res.setRetentionTime(authScoring.getRetentionTime());
        res.setBrightnessCheck(Boolean.TRUE.equals(authScoring.getBrightness()));
        res.setGenderCheck(Boolean.TRUE.equals(authScoring.getGenderCheck()));
        res.setRaceCheck(Boolean.TRUE.equals(authScoring.getRaceCheck()));
        res.setAgeCheck(Boolean.TRUE.equals(authScoring.getAgeCheck()));
        res.setFaceDetectionCheck(Boolean.TRUE.equals(authScoring.getFaceDetection()));
        res.setOnePersonCheck(Boolean.TRUE.equals(authScoring.getOnePerson()));
        res.setLiveness(authScoring.getLivenessType());
        res.setAuthenticationEffectiveDistance(authScoring.getAuthenticationEffectiveDistanceValue());
        res.setTwoStepVerification(authScoring.getTwoStepVerificationType());
        res.setThirdPartyCheck(authScoring.getThirdPartyRecognitionMethods() != null
            && !authScoring.getThirdPartyRecognitionMethods().isEmpty());
        res.setRecognitionImageSavingCheck(Boolean.TRUE.equals(authScoring.getRecognitionImageSaving()));
        res.setTwoStepActive(authScoring.getIsTwoStepActive());
        res.setAutoAdjustFaceSize(Boolean.TRUE.equals(authScoring.getAutoAdjustFaceSize()));
        res.setAuthenticationSizeType(authScoring.getAuthenticationSizeType());
        res.setAuthenticationSizeValue(authScoring.getAuthenticationSizeValue());
        return res;
    }

    private String getAuthenticationCacheKey(String clientId) {
        return REDIS_CAMERA_PREFIX + clientId;
    }

    public Optional<Schedule> getSchedule(Set<Long> scheduleIds) {
        List<Schedule> schedules = new ArrayList<>();
        for (Long scheduleId : scheduleIds) {
            String scheduleCacheKey = REDIS_SCHEDULES + scheduleId;
            Schedule schedule = redisTemplateSchedule.opsForValue().get(scheduleCacheKey);
            if (schedule == null) {
                schedule = scheduleQueries.getSchedulesByScheduleId(scheduleId)
                    .orElseThrow(() -> new NotFoundException("Schedule not found"));
                schedule.setNeedToCache(true);
                redisTemplateSchedule.opsForValue()
                    .set(scheduleCacheKey, schedule, authenticationService.getRedisCameraTimeout());
                log.info("----- Save new schedule cache by key {}", scheduleCacheKey);
            } else {
                schedule.setNeedToCache(false);
            }
            schedules.add(schedule);
        }
        return scheduleService.filterValidSchedule(schedules);
    }

    public AuthScoring getAuthScoring(Long authScoringId) {
        String authScoringCacheKey = REDIS_AUTH_SCORING_PREFIX + authScoringId;
        AuthScoring authScoring = redisTemplateAuthScoring.opsForValue().get(authScoringCacheKey);
        if (authScoring == null) {
            authScoring = authScoringQueries.getAuthScoringById(authScoringId).orElseThrow(
                () -> new InnerServerErrorException(MessageHelper.getMessage("auth_scoring_not_found")));
            redisTemplateAuthScoring.opsForValue()
                .set(authScoringCacheKey, authScoring, authenticationService.getRedisCameraTimeout());
            log.info("----- Save new auth scoring cache by key {}", authScoringCacheKey);
        }
        return authScoring;
    }

    public AuthenticationConfigResDto getAuthenticationConfigStreamCamera(Long cameraId, Long locationId, String codeChallenge, String deviceIdentity) {
        //Find auth scoring
        OAuth2GrantSummary oAuth2Grant =
            oAuth2Queries.findOAuth2GrantSummaryByCodeChallenge(codeChallenge).orElseThrow(
                () -> new UnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        AuthenticationConfigResDto authenticationConfigResDto = new AuthenticationConfigResDto();
        AuthenticationSettingDto authenticationSettings = getCameraConfigStreamCamera(cameraId, locationId);
        authenticationConfigResDto.setAuthenticationSettings(authenticationSettings);

        if (Boolean.TRUE.equals(oAuth2Grant.getIsSuite())) {
            // Handle default authentication
            SuiteCustomizeAuthenticationDto suiteDefaultAuthenticationDto =
                accountsServiceClient.getDefaultAuthentication(locationId, oAuth2Grant.getServiceId());
            if (suiteDefaultAuthenticationDto != null) {
                applyAuthenticationDtoToResponse(suiteDefaultAuthenticationDto, authenticationConfigResDto);
                authenticationConfigResDto.setAuthenticationTimeout(suiteDefaultAuthenticationDto.getAuthenticationTimeout());
            }

            // Handle customize authentication if available
            if (oAuth2Grant.getCustomizeAuthenticationId() != null) {
                SuiteCustomizeAuthenticationDto suiteCustomizeAuthenticationDto =
                    accountsServiceClient.getCustomizeAuthentication(oAuth2Grant.getCustomizeAuthenticationId());
                if (suiteCustomizeAuthenticationDto == null) {
                    throw new NotFoundException(MessageHelper.getMessage("suite_customize_authentication_not_found"));
                }
                applyAuthenticationDtoToResponse(suiteCustomizeAuthenticationDto, authenticationConfigResDto);
                authenticationConfigResDto.setCustomizeAuthenticationId(oAuth2Grant.getCustomizeAuthenticationId());
            }
        }
        if (Boolean.TRUE.equals(authenticationConfigResDto.getIsNotificationWebhookSendUnregistered())) {
            authenticationConfigResDto.setIsNotificationWebhookSendUnregistered(
                getStatusUnknownLog(locationId));
        }
        //Find auth scoring
        Long authenticationObjId = oAuth2Grant.getAuthenticationObjId();
        if (authenticationObjId == null) return authenticationConfigResDto;
        ServiceSummary service = serviceQueriesService.findSummaryServiceById(oAuth2Grant.getServiceId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(
                "service_not_found")));
        Map<String, List<AuthenticationObjDetails>> authenticationObjItems = getAuthenticationObjItemsByAuthObjId(authenticationObjId);
        AuthScoringSettingDto authScoringSettingDto;
        if (service.equals(ServiceType.AUTH.name())) {
            authScoringSettingDto = getScheduleValues(Set.of(authenticationObjId));
        } else {
            authScoringSettingDto = getAuthScoringByAuthenticationObjItems(authenticationObjItems);
        }
        AuthenticationGps authenticationGps = getGpsByAuthenticationObjItems(authenticationObjItems);
        authenticationConfigResDto.setGpsLat(authenticationGps == null ?
            0D : authenticationGps.getGpsLat());
        authenticationConfigResDto.setGpsLong(authenticationGps == null ?
            0D : authenticationGps.getGpsLong());
        authenticationConfigResDto.setIsRegisteredDeviceValid(
            isRegisteredDeviceValid(authScoringSettingDto == null ? null :authScoringSettingDto.getId(), deviceIdentity)
        );
        if (service.getType().equals(ServiceType.AUTH.name())) {
            authenticationConfigResDto.setAuthScoringSettingsForAuth(authScoringSettingDto);
        } else {
            authenticationConfigResDto.setAuthScoringSettings(authScoringSettingDto);
        }
        return authenticationConfigResDto;
    }

    public AuthenticationSettingDto getCameraConfigStreamCamera(Long cameraId, Long locationId) {
        String redisKey = REDIS_STREAM_CAMERA_PREFIX + cameraId;
        AuthenticationSettingDto authenticationSettings = redisTemplateAuthentication.opsForValue().get(redisKey);
        if (authenticationSettings == null || ObjectUtils.isEmpty(authenticationSettings.getCameraName())) {
            AuthenticationSettingSummary authenticationSetting =
                authenticationSettingQueriesService.findAuthenticationSettingSummaryById(cameraId).orElseThrow(
                    () -> new NotFoundException(MessageHelper.getMessage("authentication_setting_not_found")));

            PlanSummary plan = planQueriesService.findPlanSummaryByAuthenticationCameraIdAndLocationId(
                cameraId, locationId, TimeUtils.currentDate()).orElseThrow(
                () -> new NotFoundException(MessageHelper.getMessage("plan_not_found")));

            List<String> aiSettings =
                authenticationSettingQueriesService.findAuthenticationAiSettingsByAuthenticationSettingId(
                    cameraId).stream().map(AuthenticationAiSetting::getType).toList();
            List<String> faceDirections =
                authenticationSettingQueriesService.findAuthenticationFaceDirectionsByAuthenticationSettingId(
                    authenticationSetting.getId()).stream().map(AuthenticationFaceDirection::getType).toList();

            Map<String, AuthenticationSettingItem> authenticationSettingItems =
                authenticationSettingQueriesService.findAuthenticationSettingItemsByAuthenticationSettingId(
                        authenticationSetting.getId()).stream()
                    .collect(Collectors.toMap(AuthenticationSettingItem::getAuthenticationSettingField, item -> item));

            Boolean isNotificationWebhookSendUnregistered =
                authenticationSettingQueriesService.findAuthenticationWebhooksByAuthenticationSettingId(authenticationSetting.getId())
                    .stream()
                    .anyMatch(AuthenticationWebhookSettingSummary::getSendUnregistered);

            authenticationSettings = AuthenticationSettingDto.builder()
                .aiSettings(aiSettings)
                .numberOfFaceRegistered(faceDirections)
                .authenticationInterval(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_INTERVAL.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_INTERVAL.name()).getValue()))
                .authenticationMinWidth(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_WIDTH.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_WIDTH.name())
                                .getValue()))
                .authenticationMinHeight(
                    authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_HEIGHT.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.AUTHENTICATION_MIN_HEIGHT.name())
                                .getValue()))
                .isLogRecognize(
                    authenticationSettingItems.get(AuthenticationFields.IS_LOG_RECOGNIZE.name()) != null &&
                        Boolean.parseBoolean(
                            authenticationSettingItems.get(AuthenticationFields.IS_LOG_RECOGNIZE.name())
                                .getValue()))
                .enabledGps(authenticationSettingItems.get(AuthenticationFields.ENABLED_GPS.name()) == null ||
                    Boolean.parseBoolean(authenticationSettingItems.get(AuthenticationFields.ENABLED_GPS.name()).getValue()))
                .healthCheck(
                    authenticationSettingItems.get(AuthenticationFields.HEALTH_CHECK.name()) == null ? null :
                        Integer.parseInt(
                            authenticationSettingItems.get(AuthenticationFields.HEALTH_CHECK.name())
                                .getValue()))
                .cameraId(authenticationSetting.getId())
                .cameraName(authenticationSetting.getCameraName())
                .repositoryId(plan.getAiRepoId())
                .isNotificationWebhookSendUnregistered(isNotificationWebhookSendUnregistered)
                .build();
            redisTemplateAuthentication.opsForValue().set(redisKey, authenticationSettings, authenticationService.getRedisCameraTimeout());
            log.info("----- Save new oauth cache by key {}", redisKey);
        }
        return authenticationSettings;
    }

    public AuthScoringSettingDto getAuthScoringByCodeChallenge(String codeChallenge) {
        OAuth2GrantSummary oAuth2Grant =
            oAuth2Queries.findOAuth2GrantSummaryByCodeChallenge(codeChallenge).orElseThrow(
                () -> new UnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        Long authenticationObjId = oAuth2Grant.getAuthenticationObjId();
        if (authenticationObjId == null) return null;
        ServiceSummary service = serviceQueriesService.findSummaryServiceById(oAuth2Grant.getServiceId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(
                "service_not_found")));
        return getAuthScoringByAuthenticationObjId(authenticationObjId, service.getType());
    }

    public AuthScoringSettingDto getAuthScoringByOAuth2Grant(OAuth2GrantSummary oAuth2Grant) {
        Long authenticationObjId = oAuth2Grant.getAuthenticationObjId();
        if (authenticationObjId == null) return null;
        ServiceSummary service = serviceQueriesService.findSummaryServiceById(oAuth2Grant.getServiceId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(
                "service_not_found")));
        return getAuthScoringByAuthenticationObjId(authenticationObjId, service.getType());
    }

    public AuthScoringSettingDto getAuthScoringByAuthenticationObjId(Long authenticationObjId, String service) {
        if (service.equals(ServiceType.AUTH.name())) {
            return getScheduleValues(Set.of(authenticationObjId));
        }
        List<AuthenticationObjDetails> authenticationObjDetails = authenticationObjQueries.findAllItemsByObjId(authenticationObjId);
        Map<String, List<AuthenticationObjDetails>> authenticationObjItems =
            authenticationObjDetails.stream().collect(Collectors.groupingBy(AuthenticationObjDetails::getType));
        return getAuthScoringByAuthenticationObjItems(authenticationObjItems);
    }

    public Map<String, List<AuthenticationObjDetails>> getAuthenticationObjItemsByAuthObjId(Long authenticationObjId) {
        List<AuthenticationObjDetails> authenticationObjDetails = authenticationObjQueries.findAllItemsByObjId(authenticationObjId);
        return authenticationObjDetails.stream().collect(Collectors.groupingBy(AuthenticationObjDetails::getType));
    }

    public AuthScoringSettingDto getAuthScoringByAuthenticationObjItems(Map<String, List<AuthenticationObjDetails>> authenticationObjItems) {
        if (!authenticationObjItems.isEmpty() &&
            authenticationObjItems.containsKey(AuthenticationObjItemField.SCHEDULE_ID.name())) {
            Set<Long> scheduleIds =
                authenticationObjItems.get(AuthenticationObjItemField.SCHEDULE_ID.name()).stream().map(
                    AuthenticationObjDetails::getValue).map(Long::parseLong).collect(Collectors.toSet());
            Boolean restrictByGps =
                Boolean.valueOf(authenticationObjItems.get(AuthenticationObjItemField.RESTRICT_BY_GPS.name())
                    .get(0)
                    .getValue());
            if (Boolean.TRUE.equals(restrictByGps)) {
                Double latitude = Double.valueOf(authenticationObjItems.get(AuthenticationObjItemField.LATITUDE.name())
                    .get(0)
                    .getValue());
                Double longitude =
                    Double.valueOf(authenticationObjItems.get(AuthenticationObjItemField.LONGITUDE.name())
                        .get(0)
                        .getValue());
            }
            return getScheduleValues(scheduleIds);
        }
        return null;
    }

    public AuthenticationGps getGpsByAuthenticationObjItems(Map<String, List<AuthenticationObjDetails>> authenticationObjItems) {
        Boolean restrictByGps =
            Boolean.valueOf(authenticationObjItems.get(AuthenticationObjItemField.RESTRICT_BY_GPS.name())
                .get(0)
                .getValue());
        if (Boolean.TRUE.equals(restrictByGps)) {
            Double latitude = Double.valueOf(authenticationObjItems.get(AuthenticationObjItemField.LATITUDE.name())
                .get(0)
                .getValue());
            Double longitude =
                Double.valueOf(authenticationObjItems.get(AuthenticationObjItemField.LONGITUDE.name())
                    .get(0)
                    .getValue());
            return new AuthenticationGps(latitude, longitude);
        }
        return null;
    }

    public Optional<AuthenticationSettingSummary> findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(
        String serviceType, Long locationId) {
        return authenticationSettingQueriesService.findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(
            serviceType, locationId, CameraType.STREAM_CAMERA.name());
    }

    public boolean getStatusUnknownLog(Long locationId) {
        return Boolean.TRUE.equals(companyQueries.findCompanyDetailsByLocationId(locationId).getUseUnknownLog());
    }

    public Boolean isRegisteredDeviceValid(Long authScoringId, String deviceIdentity) {
        if (authScoringId == null) {
            return null;
        }
        var devices = authScoringQueriesService.findDeviceRegistrationsByAuthScoringId(authScoringId);
        var staticDevices = authScoringQueriesService.findAuthScoringDevicesByAuthScoringId(authScoringId);
        // no restrict devices
        if (staticDevices.isEmpty() && devices.isEmpty()) {
            return true;
        }
        // restrict devices but no device registrations
        if (devices.isEmpty()) {
            return null;
        }
        if (StringUtils.isEmpty(deviceIdentity)) {
            return false;
        }
        return devices.stream().anyMatch(d -> d.getDeviceIdentity().equals(deviceIdentity)
            && d.getStatus() == DeviceRegistrationStatus.APPROVED.getValue());
    }

    private void applyAuthenticationDtoToResponse(
        SuiteCustomizeAuthenticationDto dto,
        AuthenticationConfigResDto responseDto
    ) {
        responseDto.setAuthenticationMinWidth(dto.getAuthenticationMinWidth());
        responseDto.setAuthenticationMinHeight(dto.getAuthenticationMinHeight());

        boolean isSendUnregistered = dto.getWebhooks() != null &&
            dto.getWebhooks().stream()
                .anyMatch(webhook -> Boolean.TRUE.equals(webhook.getSendUnregistered()));
        responseDto.setIsNotificationWebhookSendUnregistered(isSendUnregistered);

        List<Long> aiSettingIds = dto.getAiSettingIds() != null
            ? dto.getAiSettingIds().stream().toList()
            : List.of();

        List<String> aiSettingTypes = aiSettingQueriesService.findByIds(aiSettingIds).stream()
            .map(AiSetting::getType)
            .toList();

        responseDto.setAiSettings(aiSettingTypes);
    }
}
