package com.wiredpackage.oauth.api.controllers;

import an.awesome.pipelinr.Pipeline;
import com.wiredpackage.auth.shared.constants.Oauth2GrantStatus;
import com.wiredpackage.oauth.api.application.commands.auth.login_command.LoginCommand;
import com.wiredpackage.oauth.api.application.commands.face.log_false_positive.LogFalsePositiveCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.get_oauth_code_command.GenerateOAuthCodeCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.log_recognize_image_command.LogRecognizeImageCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.perform_approval_command.PerformApprovalCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.perform_self_cancel_approval_command.PerformSelfCancelApprovalCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.request_oauth_command.RequestOAuthUrlCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.send_code_verify_command.SendCodeVerifyCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.upload_multi_recognize_face_log_command.UploadMultiRecognizeFaceLogCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.upload_recognize_face_log_command.UploadRecognizeFaceLogCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.verify_face_command.VerifyFaceCommand;
import com.wiredpackage.oauth.api.application.commands.oauth.verify_face_command.VerifyFaceStreamCommand;
import com.wiredpackage.oauth.api.application.models.auth.*;
import com.wiredpackage.oauth.api.application.models.authentication.AuthenticationSettingSummary;
import com.wiredpackage.oauth.api.application.models.company.CompanyDetails;
import com.wiredpackage.oauth.api.application.models.identity.IdentitySummary;
import com.wiredpackage.oauth.api.application.queries.authentication.AuthenticationQueries;
import com.wiredpackage.oauth.api.application.queries.company.CompanyQueries;
import com.wiredpackage.oauth.api.application.queries.identity.IdentityQueries;
import com.wiredpackage.oauth.api.application.queries.oauth.OAuth2Queries;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.application.services.AuthenticationWebhookService;
import com.wiredpackage.oauth.api.application.services.OAuthLogService;
import com.wiredpackage.oauth.api.dto.auth.LoginResDto;
import com.wiredpackage.oauth.api.dto.authentication.AuthScoringSettingDto;
import com.wiredpackage.oauth.api.dto.oauth.*;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_grant_aggregate.OAuth2Grant;
import com.wiredpackage.oauth.infrastructure.repositories.OAuth2GrantRepository;
import com.wiredpackage.oauth.shared.constants.FalsePositiveType;
import com.wiredpackage.oauth.shared.helpers.BrightnessHelper;
import com.wiredpackage.shared.application.dto.ApiResponse;
import com.wiredpackage.shared.application.dto.UserDetail;
import com.wiredpackage.shared.application.dto.oauth_service.LogRecognizeResDto;
import com.wiredpackage.shared.application.exceptions.TaopassInnerServerErrorException;
import com.wiredpackage.shared.application.exceptions.TaopassUnauthorizationException;
import com.wiredpackage.shared.application.security.SecurityService;
import com.wiredpackage.shared.dto.WaitingApprovalResDto;
import com.wiredpackage.shared.infrastructure.services.OAuthAppService;
import com.wiredpackage.shared.infrastructure.services.PKCEService;
import com.wiredpackage.shared.shared.constants.CameraType;
import com.wiredpackage.shared.shared.constants.DefaultAccountAuthSettings;
import com.wiredpackage.shared.shared.constants.Oauth2GrantType;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.wiredpackage.auth.shared.constants.Oauth2GrantStatus.DONE;

@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "oauth")
@RequestMapping("oauth")
@RestController
public class OAuthController {
    private final Pipeline pipeline;
    private final OAuth2Queries oAuth2Queries;
    private final IdentityQueries identityQueries;
    private final PKCEService pkceService;
    private final OAuthAppService oAuthAppService;
    private final AuthService authService;
    private final CompanyQueries companyQueries;
    private final SecurityService securityService;
    private final OAuth2GrantRepository oAuth2GrantRepository;
    private final AuthenticationQueries authenticationQueries;
    private final BrightnessHelper brightnessHelper;
    private final OAuthLogService oAuthLogService;
    private final AuthenticationWebhookService authenticationWebhookService;

    @PostMapping
    public RequestOAuthResDto requestOAuth(@Valid @RequestBody RequestOAuthReqDto request) {
        String cameraType = CameraType.STREAM_CAMERA.name().equals(request.getCameraType()) ?
            CameraType.STREAM_CAMERA.name() : CameraType.OAUTH.name();
        authService.checkCertificationLimit(request.getService(), request.getLocationId(), cameraType);
        RequestOAuthUrlCommand command = RequestOAuthUrlCommand.builder()
            .codeChallenge(request.getCodeChallenge())
            .state(request.getState())
            .locationId(request.getLocationId())
            .service(request.getService())
            .cameraType(cameraType)
            .authenticationType(Oauth2GrantType.valueOf(request.getAuthenticationType()))
            .authObjId(request.getAuthObjId())
            .isSuite(request.getIsSuite())
            .isLocation(request.getIsLocation())
            .customizeAuthenticationId(request.getCustomizeAuthenticationId())
            .build();
        String url = pipeline.send(command);
        return RequestOAuthResDto.builder()
            .url(url)
            .build();
    }

    @PostMapping("logs/{codeChallenge}")
    public void recognizeFaceLog(@PathVariable("codeChallenge") String codeChallenge,
                                 MultipartFile file,
                                 @RequestPart(name = "faceBox", required = false) FaceBoxDto faceBox,
                                 @RequestPart("meta") OAuthLogMetaReqDto meta) {
        OAuth2GrantSummary oAuth2Grant =
            oAuth2Queries.findOAuth2GrantSummaryByCodeChallenge(codeChallenge).orElseThrow(
                () -> new TaopassUnauthorizationException(
                    MessageHelper.getMessage("invalid_code_challenge_or_expired")));
        String logImageObjectKey = "";
        AuthScoringSettingDto authScoringSettingDto = authenticationQueries.getAuthScoringByOAuth2Grant(oAuth2Grant);
        try {
            if (file != null && (Objects.isNull(authScoringSettingDto) || authScoringSettingDto.isRecognitionImageSavingCheck())) {
                logImageObjectKey = oAuthAppService.asyncUploadLogImage(file);
            }
        } catch (Exception e) {
            log.error("Failed to upload log image", e);
            throw new TaopassInnerServerErrorException();
        }
        Double brightness = brightnessHelper.getBrightness(file, faceBox);
        UploadRecognizeFaceLogCommand command = UploadRecognizeFaceLogCommand.builder()
            .oAuthGrant(oAuth2Grant)
            .identityId(meta.getIdentityId())
            .objectKey(logImageObjectKey)
            .age(meta.getAge())
            .gender(meta.getGender())
            .gps(meta.getGps())
            .brightness(brightness)
            .emotions(meta.mapEmotions())
            .racial(meta.getRacial())
            .cosineSimilarity(meta.getCosineSimilarity())
            .bbox(faceBox == null?
                Collections.emptyList(): List.of(faceBox.getMinX(), faceBox.getMinY(), faceBox.getMaxX(), faceBox.getMaxY()))
            .build();
        pipeline.send(command);
    }

    @PostMapping(value = "get-brightness")
    public Double getBrightness(MultipartFile file,
                                @RequestPart(name = "faceBox", required = false) FaceBoxDto faceBox
    ) {

        return brightnessHelper.getBrightness(file, faceBox);
    }

    @PostMapping(value = "log-false-positive", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void logFalsePositive(@RequestParam(name = "image") MultipartFile image,
                                 @RequestParam(name = "codeChallenge") String codeChallenge,
                                 @RequestParam(name = "compareFaceId") Long compareFaceId,
                                 @RequestParam(name = "recognizeFaceId") Long recognizeFaceId,
                                 @RequestParam(name = "falsePositiveType") FalsePositiveType falsePositiveType) {

        LogFalsePositiveCommand logFalsePositiveCommand = LogFalsePositiveCommand.builder()
            .compareFaceId(compareFaceId)
            .recognizeFaceId(recognizeFaceId)
            .codeChallenge(codeChallenge)
            .image(image)
            .falsePositiveType(falsePositiveType)
            .build();
        pipeline.send(logFalsePositiveCommand);
    }

    @GetMapping("status/{codeChallenge}")
    public boolean checkActiveCodeChallenge(@PathVariable("codeChallenge") String codeChallenge) {
        OAuth2GrantSummary oAuth2Grant =
            oAuth2Queries.findOAuth2GrantSummaryByCodeChallenge(codeChallenge).orElse(null);
        return oAuth2Grant != null;
    }

    @PostMapping("verify")
    public VerifyFaceResDto verifyFace(@RequestBody VerifyFaceReqDto request) {

        OAuth2Grant oAuth2Grant = oAuth2GrantRepository.findByCodeChallenge(request.getCodeChallenge())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));

        if (oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.STREAM.name())) {
            return VerifyFaceResDto.builder()
                .verifyType(Oauth2GrantStatus.DONE.name())
                .build();
        }
        Long authenticationSettingId;
        IdentitySummary identity;

        if (oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.ACCOUNT_RECOGNIZANCE.name())
            || oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.ACCOUNT_REGISTRATION.name())
            || oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.STREAM_RECOGNIZANCE.name())
            || oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.TIGEREYE_BOX_STREAM_RECOGNIZANCE.name())
            || oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.REGISTER_AGAIN.name())) {

            Optional<IdentitySummary> identityOptional = identityQueries.findIdentitySummaryByFaceId(request.getFaceId());
            if (identityOptional.isEmpty()) {
                log.error("----- Identity not found with faceId {}", request.getFaceId());
                throw new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found"));
            }
            authenticationSettingId = DefaultAccountAuthSettings.DEFAULT_AUTHENTICATION_SETTING_ID;
            if (oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.STREAM_RECOGNIZANCE.name())
                || oAuth2Grant.getOauthGrantType().equals(Oauth2GrantType.TIGEREYE_BOX_STREAM_RECOGNIZANCE.name())) {
                authenticationSettingId = getAuthenticationSettingStreamFromLocationAndService(request.getService(), request.getLocationId());
            }
            GenerateOAuthCodeCommand command = GenerateOAuthCodeCommand.builder()
                .faceId(request.getFaceId())
                .identityId(identityOptional.get().getId())
                .oAuthGrantId(oAuth2Grant.getId())
                .authenticationSettingId(authenticationSettingId)
                .locationId(oAuth2Grant.getLocationId())
                .build();
            String code = pipeline.send(command);
            return VerifyFaceResDto.builder()
                .verifyType(DONE.name())
                .data(Map.of("code", code))
                .build();
        }
        AuthenticationSettingSummary authenticationSetting =
            oAuth2Queries.findAuthenticationSettingSummaryByClientId(oAuth2Grant.getClientId()).orElseThrow(
                () -> new TaopassUnauthorizationException(MessageHelper.getMessage("authentication_setting_not_found")));
        Optional<IdentitySummary> identityOptional = identityQueries.findIdentityByFaceIdAndCompanyId(request.getFaceId(),
            authenticationSetting.getCompanyId());
        if (identityOptional.isEmpty()) {
            log.error("----- Identity not found with faceId {}, company {} from camera {}", request.getFaceId(),
                authenticationSetting.getCompanyId(), authenticationSetting.getCameraName());
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found"));
        }
        identity = identityOptional.get();
        log.info("----- Identity found with faceId {}, company {} from camera {}", request.getFaceId(),
            authenticationSetting.getCompanyId(), authenticationSetting.getCameraName());
        if (!authenticationSetting.getCompanyId().equals(identity.getCompanyId())) {
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("identity_is_not_valid"));
        }

        VerifyFaceCommand command = VerifyFaceCommand.builder()
            .verifyFaceReqDto(request)
            .oAuth2Grant(oAuth2Grant)
            .authenticationSettingId(authenticationSetting.getId())
            .identity(identity)
            .service(request.getService())
            .isLogRecognize(request.getIsLogRecognize())
            .deviceIdentity(request.getDeviceIdentity())
            .build();
        return pipeline.send(command);
    }

    @PostMapping("verify-stream")
    public VerifyFaceResDto verifyFaceStream(@RequestBody VerifyFaceReqDto request) {
        Optional<IdentitySummary> identityOptional = identityQueries.findIdentitySummaryByFaceId(request.getFaceId());
        if (identityOptional.isEmpty()) {
            log.error("----- Identity not found with faceId {}", request.getFaceId());
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found"));
        }
        IdentitySummary identity = identityOptional.get();
        Long authenticationSettingId = getAuthenticationSettingStreamFromLocationAndService(request.getService(), request.getLocationId());
        VerifyFaceStreamCommand verifyFaceStreamCommand = VerifyFaceStreamCommand.builder()
            .verifyFaceReqDto(request)
            .oAuth2LogId(request.getOAuth2LogId())
            .authenticationSettingId(authenticationSettingId)
            .identity(identity)
            .service(request.getService())
            .isLogRecognize(request.getIsLogRecognize())
            .deviceIdentity(request.getDeviceIdentity())
            .build();
        return pipeline.send(verifyFaceStreamCommand);
    }

    private Long getAuthenticationSettingStreamFromLocationAndService(String serviceType, Long locationId) {
        return authenticationQueries.findAuthenticationSettingByServiceTypeAndLocationIdAndCameraType(serviceType, locationId)
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("authentication_setting_not_found")))
            .getId();
    }

    @PostMapping("token")
    public LoginResDto getToken(@RequestBody OAuthGetTokenReqDto request) throws NoSuchAlgorithmException {
        OAuthAuthenticationSummary oAuthAuthentication = oAuth2Queries.findOAuthAuthenticationSummaryByCode(request.getCode())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_authentication_not_found")));
        OAuthGrantSummary oAuthGrant = oAuth2Queries.findOAuthGrantSummaryById(oAuthAuthentication.getOAuth2GrantId())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        if (!pkceService.verifyCodeVerifier(oAuthGrant.getCodeChallenge(), request.getCodeVerifier())) {
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("code_verifier_invalid"));
        }
        IdentityLogin identityLogin;
        if (Boolean.TRUE.equals(request.getManagerLoginFace())) {
            // get company by identity id
            CompanyDetails company = companyQueries.findCompanyDetailsByIdentityId(oAuthAuthentication.getIdentityId());
            // find identity id manager
            identityLogin = identityQueries.findIdentityManagerByIdentityUser(oAuthAuthentication.getIdentityId(), company.getId())
                .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found")));

        } else {
            identityLogin = identityQueries.findIdentityByIdentityId(oAuthAuthentication.getIdentityId())
                .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found")));
        }
        //get gps from oauth2Log
        String gps;
        try {
            OAuthLogSummary logSummary = oAuth2Queries.getOAuthLogWithCodeChallenge(oAuthGrant.getCodeChallenge());
            gps = logSummary.getGps();
        } catch (Exception e) {
            gps = null;
        }
        LoginCommand command = LoginCommand.builder()
            .identityLogin(identityLogin)
            .oauth2AuthenticationId(oAuthAuthentication.getId())
            .gps(gps)
            .build();
        return pipeline.send(command);
    }

    @PostMapping("token-stream")
    public LoginResDto getTokenWithoutExpiring(@RequestBody OAuthGetTokenReqDto request) throws NoSuchAlgorithmException {
        OAuthAuthenticationSummary oAuthAuthentication = oAuth2Queries.findOAuthAuthenticationSummaryByCode(request.getCode())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_authentication_not_found")));
        OAuthGrantSummary oAuthGrant = oAuth2Queries.findOAuthGrantSummaryById(oAuthAuthentication.getOAuth2GrantId())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        if (!pkceService.verifyCodeVerifier(oAuthGrant.getCodeChallenge(), request.getCodeVerifier())) {
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("code_verifier_invalid"));
        }
        IdentityLogin identityLogin;
        if (Boolean.TRUE.equals(request.getManagerLoginFace())) {
            // get company by identity id
            CompanyDetails company = companyQueries.findCompanyDetailsByIdentityId(oAuthAuthentication.getIdentityId());
            // find identity id manager
            identityLogin = identityQueries.findIdentityManagerByIdentityUser(oAuthAuthentication.getIdentityId(), company.getId())
                .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found")));

        } else {
            identityLogin = identityQueries.findIdentityByIdentityId(oAuthAuthentication.getIdentityId())
                .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found")));
        }
        //get gps from oauth2Log
        String gps;
        try {
            OAuthLogSummary logSummary = oAuth2Queries.getOAuthLogWithCodeChallenge(oAuthGrant.getCodeChallenge());
            gps = logSummary.getGps();
        } catch (Exception e) {
            gps = null;
        }
        LoginCommand command = LoginCommand.builder()
            .identityLogin(identityLogin)
            .oauth2AuthenticationId(oAuthAuthentication.getId())
            .gps(gps)
            .build();
        return pipeline.send(command);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("approval")
    public void performWaitingApproval(@RequestBody PerformApprovalReqDto request) {
        UserDetail userDetail = securityService.getUserDetail();
        log.info("----- PerformApprovalCommandHandler.handle() - oAuth2WaitingApprovalId: {}",
            request.getWaitingApprovalItemId());
        PerformApprovalCommand command = PerformApprovalCommand.builder()
            .waitingApprovalItemId(request.getWaitingApprovalItemId())
            .isApproved(request.getIsApproved())
            .identityId(userDetail.getId())
            .authorityId(userDetail.getAuthorityId())
            .build();
        pipeline.send(command);
        log.info("----- PerformApprovalCommandHandler.handle() send ApprovedEvent - oAuth2WaitingApprovalId: {}",
            request.getWaitingApprovalItemId());
    }

    @PostMapping("self-cancel-approval")
    public void performSelfCancel(@RequestBody PerformSelfCancelApprovalReqDto reqDto) {
        PerformSelfCancelApprovalCommand command = PerformSelfCancelApprovalCommand.builder()
            .waitingApprovalItemId(reqDto.getWaitingApprovalItemId())
            .codeChallenge(reqDto.getCodeChallenge())
            .locationId(reqDto.getLocationId())
            .service(reqDto.getService())
            .build();
        pipeline.send(command);
        log.info("---- User self canceled the waiting approval: {}", reqDto.getWaitingApprovalItemId());
    }

    @GetMapping("log-grant")
    public OAuthLogSummary getOAuthLog(@RequestParam("codeChallenge") String codeChallenge) {
        return oAuth2Queries.getOAuthLogWithCodeChallenge(codeChallenge);
    }

    @PostMapping("send-code-verify")
    public Long sendCodeVerify(@RequestBody SendCodeVerifyDto request) {
        SendCodeVerifyCommand command = SendCodeVerifyCommand.builder()
            .codeChallenge(request.getCodeChallenge())
            .faceId(request.getFaceId())
            .typeVerify(request.getTypeVerify())
            .build();
        return pipeline.send(command);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("waiting-approvals")
    public List<WaitingApprovalResDto> getWaitingApprovals(@RequestParam("service") String service,
                                                           @RequestParam("locationId") Long locationId) {
        UserDetail userDetail = securityService.getUserDetail();
        return oAuth2Queries.getWaitingApprovals(service, locationId, userDetail.getAuthorityId(), userDetail.getId());
    }

    @PostMapping("multi-logs/{codeChallenge}")
    public Map<Long, Long> recognizeMultiFaceLog(@PathVariable("codeChallenge") String codeChallenge,
                                                 MultipartFile file,
                                                 @RequestPart("metas") List<OAuthLogMetaReqDto> metas) {
        OAuth2GrantSummary oAuth2Grant =
            oAuth2Queries.findOAuth2GrantSummaryByCodeChallenge(codeChallenge).orElseThrow(
                () -> new TaopassUnauthorizationException(
                    MessageHelper.getMessage("invalid_code_challenge_or_expired")));
        String logImageObjectKey = "";
        AuthScoringSettingDto authScoringSettingDto = authenticationQueries.getAuthScoringByOAuth2Grant(oAuth2Grant);
        try {
            if (file != null && (Objects.isNull(authScoringSettingDto) || authScoringSettingDto.isRecognitionImageSavingCheck())) {
                logImageObjectKey = oAuthAppService.asyncUploadLogImage(file);
            }
        } catch (Exception e) {
            log.error("Failed to upload log image", e);
            throw new TaopassInnerServerErrorException();
        }
        UploadMultiRecognizeFaceLogCommand command = UploadMultiRecognizeFaceLogCommand.builder()
            .oAuthGrant(oAuth2Grant)
            .objectKey(logImageObjectKey)
            .brightness(brightnessHelper.getBrightness(file))
            .listAuthLogMeta(metas)
            .build();
        return pipeline.send(command);
    }

    @PostMapping("log-recognize-image/{codeChallenge}")
    public ApiResponse<List<LogRecognizeResDto>> logImageCodeChallenge(@PathVariable("codeChallenge") String codeChallenge,
                                                                 MultipartFile file) {
        LogRecognizeImageCommand command = LogRecognizeImageCommand.builder()
            .codeChallenge(codeChallenge)
            .file(file)
            .build();
        return pipeline.send(command);
    }

    @PostMapping("verify-oauth-authentication")
    public LoginResDto verifyOauthAuthentication(@RequestBody OAuthGetTokenReqDto request) {
        OAuthAuthenticationSummary oAuthAuthentication = oAuth2Queries.findOAuthAuthenticationSummaryByCode(request.getCode())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_authentication_not_found")));
        OAuthGrantSummary oAuthGrant = oAuth2Queries.findOAuthGrantSummaryById(oAuthAuthentication.getOAuth2GrantId())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        if (!oAuthGrant.getCodeChallenge().equals(request.getCodeVerifier())) {
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("code_verifier_invalid"));
        }
        IdentityLogin identityLogin = identityQueries.findIdentityByIdentityId(oAuthAuthentication.getIdentityId())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("identity_not_found")));

        LoginCommand command = LoginCommand.builder()
            .identityLogin(identityLogin)
            .oauth2AuthenticationId(oAuthAuthentication.getId())
            .build();
        return pipeline.send(command);
    }

    @PostMapping("log-resolved")
    public void logResolvedAuthentication(@RequestBody LogResolvedAuthenticationDto request) throws NoSuchAlgorithmException {
        OAuthAuthenticationSummary oAuthAuthentication = oAuth2Queries.findOAuthAuthenticationSummaryByCode(request.getCode())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_authentication_not_found")));
        OAuthGrantSummary oAuthGrant = oAuth2Queries.findOAuthGrantSummaryById(oAuthAuthentication.getOAuth2GrantId())
            .orElseThrow(() -> new TaopassUnauthorizationException(MessageHelper.getMessage("oauth2_grand_not_found")));
        if (!pkceService.verifyCodeVerifier(oAuthGrant.getCodeChallenge(), request.getCodeVerifier())) {
            throw new TaopassUnauthorizationException(MessageHelper.getMessage("code_verifier_invalid"));
        }
        oAuthLogService.logResolvedAuthentication(oAuthGrant.getCodeChallenge(), request.getResolveTime(), request.getOthersTime(),
            request.getResolveAt());
    }

    @PostMapping("log-resolved-code-challenge")
    public void logResolvedAuthenticationCodeChallenge(@RequestBody LogResolvedAuthenticationCodeChallengeDto request) {
        oAuthLogService.logResolvedAuthentication(request.getCodeChallenge(), request.getResolveTime(), request.getOthersTime(),
            request.getResolveAt());
    }

    @PostMapping("notify-unknown-webhook")
    public void notifyUnknownWebhook(@RequestPart("file") MultipartFile file,
                                     @RequestPart("request") NotifyUnknownFaceReqDto request) throws IOException {
        byte[] bytes = file == null? null: file.getBytes();
        authenticationWebhookService.notifyUnknownUser(request.getCameraId(), request.getGpsLat(), request.getGpsLong(),
            request.getUnknownFaces(), bytes, request.getCustomizeAuthenticationId());
    }
}
