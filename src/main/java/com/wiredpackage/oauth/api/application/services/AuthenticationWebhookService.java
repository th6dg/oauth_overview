package com.wiredpackage.oauth.api.application.services;

import com.wiredpackage.oauth.api.application.models.auth.OauthLogSummaryNotify;
import com.wiredpackage.oauth.api.application.models.authentication.AuthenticationWebhookSettingSummary;
import com.wiredpackage.oauth.api.application.models.authentication.AuthenticationWebhookTagSummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentityTagInfo;
import com.wiredpackage.oauth.api.application.queries.authentication.IAuthenticationSettingQueriesService;
import com.wiredpackage.oauth.api.application.queries.identity.IdentityQueries;
import com.wiredpackage.oauth.api.application.queries.oauth.OAuth2Queries;
import com.wiredpackage.oauth.api.application.queries.suite_customize_authentication.ISuiteCustomizeAuthenticationQueriesService;
import com.wiredpackage.oauth.api.dto.oauth.NotifyEmotionDto;
import com.wiredpackage.oauth.api.dto.oauth.NotifyUnknownWebhookReqDto;
import com.wiredpackage.oauth.infrastructure.repositories.OAuth2LogRepository;
import com.wiredpackage.shared.application.dto.webhook_service.AuthenticationNotificationEmotionReqDto;
import com.wiredpackage.shared.application.dto.webhook_service.AuthenticationNotificationUserReqDto;
import com.wiredpackage.shared.infrastructure.services.OAuthAppService;
import com.wiredpackage.shared.infrastructure.services.S3Service;
import com.wiredpackage.shared.infrastructure.sevice_clients.webhook.WebhookClient;
import com.wiredpackage.shared.shared.helpers.RestTemplateHelper;
import com.wiredpackage.shared.shared.utils.NameUtils;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.wiredpackage.shared.shared.utils.TimeUtils.YYYYMMDDHHMMSS_SLASH;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationWebhookService {

    private final IAuthenticationSettingQueriesService authenticationSettingQueriesService;
    private final OAuth2Queries oAuth2Queries;
    private final IdentityQueries identityQueries;
    private final WebhookClient webhookClient;
    private final OAuthAppService oAuthAppService;
    private final OAuth2LogRepository oAuth2LogRepository;
    private final ISuiteCustomizeAuthenticationQueriesService suiteCustomizeAuthenticationQueriesService;

    private final String UNKNOWN = "unknown";
    private final String AUTHENTICATION = "authentication";
    private final S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.s3.presigned.expiration.face_picture}")
    private Long facePicturePresignedUrlExpiration;

    @Async
    public void notifyFoundUser(Long authenticationSettingId, Long oauth2GrantId, LocalDateTime lastNotificationAt, Long customizeId) {
        List<AuthenticationWebhookSettingSummary> webhooks = new ArrayList<>();
        List<Long> webhookSettingIds = new ArrayList<>();
        List<AuthenticationWebhookTagSummary> webhookTagSummaries = new ArrayList<>();
        //has suite customize authentication setting
        if (customizeId != null) {
            webhooks = suiteCustomizeAuthenticationQueriesService.findCustomizeWebhookByCustomizeSettingId(customizeId);
            webhookSettingIds = webhooks.stream()
                .map(AuthenticationWebhookSettingSummary::getId)
                .toList();
            webhookTagSummaries = suiteCustomizeAuthenticationQueriesService
                .findCustomizeWebhookTagsByWebhookSettingId(webhookSettingIds);
        } else {
            webhooks = authenticationSettingQueriesService.findAuthenticationWebhooksByAuthenticationSettingId(authenticationSettingId);
            webhookSettingIds = webhooks.stream()
                .map(AuthenticationWebhookSettingSummary::getId)
                .toList();
            webhookTagSummaries = authenticationSettingQueriesService
                .findAuthenticationWebhookTagsByWebhookSettingId(webhookSettingIds);
        }
        Map<Long, List<Long>> webhookTagIdMap = webhookTagSummaries
            .stream()
            .collect(Collectors.groupingBy(
                AuthenticationWebhookTagSummary::getAuthenticationWebhookSettingId,
                Collectors.mapping(AuthenticationWebhookTagSummary::getTagId, Collectors.toList()))
            );
        List<OauthLogSummaryNotify> logSummaries = oAuth2Queries.findLogsToNotify(oauth2GrantId,
            lastNotificationAt);
        Map<Long, OauthLogSummaryNotify> logSummaryMap = logSummaries.stream()
            .collect(Collectors.toMap(OauthLogSummaryNotify::getIdentityId, p -> p, (value1, value2) -> value2));
        Set<Long> identityIds = logSummaryMap.keySet();

        List<IdentityTagInfo> identityTagInfos = identityQueries.findTagsByIdentityIds(identityIds);
        Map<Long, List<Long>> identityTagMap = identityTagInfos.stream()
            .collect(Collectors.groupingBy(
                IdentityTagInfo::getIdentityId,
                Collectors.mapping(IdentityTagInfo::getTagId, Collectors.toList()))
            );
        notifyForLogSummary(webhooks, webhookTagIdMap, logSummaryMap, identityIds, identityTagMap);
    }

    @Async
    public void notifyUnknownUser(Long authenticationSettingId, String gpsLat, String gpsLong,
                                  List<NotifyUnknownWebhookReqDto> unknownFaces, byte[] bytes, Long customizeId) {
        List<AuthenticationWebhookSettingSummary> allWebhooks = customizeId != null
            ? suiteCustomizeAuthenticationQueriesService.findCustomizeWebhookByCustomizeSettingId(customizeId)
            : authenticationSettingQueriesService.findAuthenticationWebhooksByAuthenticationSettingId(authenticationSettingId);

        List<AuthenticationWebhookSettingSummary> unknownWebhooks = allWebhooks.stream()
            .filter(AuthenticationWebhookSettingSummary::getSendUnregistered)
            .toList();
        String objectKeyUrl = unknownWebhooks.isEmpty() || bytes == null ? null :
            uploadUnknownFace(authenticationSettingId, bytes);
        //tags for unknown is empty
        List<String> tagNames = Collections.emptyList();
        List<AuthenticationNotificationUserReqDto> users = unknownFaces.stream()
            .map(user -> AuthenticationNotificationUserReqDto.builder()
                .user_id(null)
                .name(null)
                .age(user.getAge())
                .gender(user.getGender())
                .bbox(user.getBbox())
                .emotion(convertEmotion(user.getEmotion()))
                .build())
            .toList();
        String date = TimeUtils.parseString(TimeUtils.now(), YYYYMMDDHHMMSS_SLASH);
        unknownWebhooks.forEach(webhook -> {
            notify(webhook.getUrl(), UNKNOWN, date, tagNames, gpsLat, gpsLong, objectKeyUrl, users, webhook.getToken());
        });
    }

    private void notifyForLogSummary(List<AuthenticationWebhookSettingSummary> webhooks,
                                     Map<Long, List<Long>> webhookTagIdMap,
                                     Map<Long, OauthLogSummaryNotify> logSummaryMap,
                                     Set<Long> identityIds,
                                     Map<Long, List<Long>> identityTagMap) {
        OauthLogSummaryNotify firstLogSummary = logSummaryMap.values()
                                                            .stream()
                                                            .sorted((x1, x2) -> x2.getAccessTime().compareTo(x1.getAccessTime()))
                                                            .findFirst()
                                                            .get();
        String objectKeyUrl = getPreSignedLogUrl(firstLogSummary.getObjectKey());
        String[] gpsInfo = splitGps(firstLogSummary.getGps());
        String date = TimeUtils.parseString(firstLogSummary.getAccessTime(), YYYYMMDDHHMMSS_SLASH);

        webhooks.forEach(webhook -> {

            logSummaryMap.values().forEach(logSummary -> {
                oAuth2LogRepository.findById(logSummary.getId())
                .ifPresent(log -> {
                    log.setIsNotify(1);;
                    oAuth2LogRepository.save(log);
                });
            });
            //send to all users
            if (Boolean.TRUE.equals(webhook.getSendAllRegistered())) {
                List<AuthenticationNotificationUserReqDto> users = logSummaryMap.values().stream()
                    .map(this::toWebhookUserRequest)
                    .toList();
                notify(webhook.getUrl(), AUTHENTICATION, date, seperateTags(webhook.getTagNames()),
                    gpsInfo[0], gpsInfo[1], objectKeyUrl, users, webhook.getToken());
                return;
            }
            //send by tags
            List<Long> webhookTagIds = webhookTagIdMap.get(webhook.getId());
            List<Long> toSendIdentityIds = new ArrayList<>();
            if (Boolean.TRUE.equals(webhook.getSendByTags())) {
                identityIds.forEach(identityId -> {
                    List<Long> identityTagIds = identityTagMap.get(identityId);
                    if (!Collections.disjoint(identityTagIds, webhookTagIds)) {
                        toSendIdentityIds.add(identityId);
                    }
                });
                List<AuthenticationNotificationUserReqDto> users = logSummaryMap.values().stream()
                    .filter(p -> toSendIdentityIds.contains(p.getIdentityId()))
                    .map(this::toWebhookUserRequest)
                    .toList();
                notify(webhook.getUrl(), AUTHENTICATION, date, seperateTags(webhook.getTagNames()),
                    gpsInfo[0], gpsInfo[1], objectKeyUrl, users, webhook.getToken());
            }
        });
    }

    private void notify(String url, String type, String date, List<String> tagNames, String latitude, String longitude,
                        String objectKeyUrl, List<AuthenticationNotificationUserReqDto> items, String token) {
        try {
            log.info("Send notify unknown user to webhook");
            if(items == null || items.isEmpty()) {
                log.debug("------ Send webhook error: items is empty");
                return;
            }
            HttpStatusCode responseCode = webhookClient.sendNotifyDataToWebhook(url, type, date, tagNames, latitude, longitude,
                objectKeyUrl, items, token == null ? null : RestTemplateHelper.createBearerToken(token));
            if (null == responseCode || !responseCode.is2xxSuccessful()) {
                log.warn("Webhook send not success");
            }
        } catch (Exception e) {
            log.warn("------ Send webhook error: {}", e.getMessage());
        }
    }

    private String[] splitGps(String gps) {
        try {
            return gps.split(",", 2);
        } catch (Exception e) {
            return new String[]{null, null};
        }
    }

    private String uploadUnknownFace(Long cameraId, byte[] bytes) {
        return oAuthAppService.uploadUnknownLogImageGenerateLink(cameraId, facePicturePresignedUrlExpiration, bytes);
    }

    private String getPreSignedLogUrl(String objectKey) {
        return s3Service.getSignedObjectUrl(bucket, objectKey, facePicturePresignedUrlExpiration);
    }

    private AuthenticationNotificationEmotionReqDto convertEmotion(NotifyEmotionDto dto) {
        return (dto == null || dto.getHappy() == null)? null:
            AuthenticationNotificationEmotionReqDto.builder()
            .happy(dto.getHappy())
            .sad(dto.getSad())
            .angry(dto.getAngry())
            .neutral(dto.getNeutral())
            .fear(dto.getFear())
            .surprised(dto.getSurprised())
            .disgust(dto.getDisgust())
            .build();
    }

    private AuthenticationNotificationUserReqDto toWebhookUserRequest(OauthLogSummaryNotify logSummary) {
        return AuthenticationNotificationUserReqDto.builder()
            .user_id(logSummary.getIdentityId().toString())
            .name(logSummary.getName())
            .age(logSummary.getAge())
            .gender(logSummary.getGender())
            .bbox(NameUtils.stringToBbox(logSummary.getBbox()))
            .emotion(logSummary.getScoreHappy() == null? null:
                AuthenticationNotificationEmotionReqDto.builder()
                .happy(logSummary.getScoreHappy())
                .sad(logSummary.getScoreSad())
                .angry(logSummary.getScoreAngry())
                .neutral(logSummary.getScoreNeutral())
                .fear(logSummary.getScoreFear())
                .surprised(logSummary.getScoreSurprised())
                .disgust(logSummary.getScoreDisgust())
                .build())
            .build();
    }

    private List<String> seperateTags(String queriedTagNames) {
        return StringUtils.isBlank(queriedTagNames)? Collections.emptyList():
            Arrays.stream(StringUtils.split(queriedTagNames, "~~~"))
                .toList();
    }
}
