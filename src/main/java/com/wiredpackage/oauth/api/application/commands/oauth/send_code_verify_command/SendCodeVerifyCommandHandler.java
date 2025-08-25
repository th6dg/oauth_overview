package com.wiredpackage.oauth.api.application.commands.oauth.send_code_verify_command;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import com.wiredpackage.auth.infrastructure.sevice_clients.sms.SmsServiceClient;
import com.wiredpackage.auth.shared.constants.AuthConstants;
import com.wiredpackage.auth.shared.constants.OAuthTwoFAType;
import com.wiredpackage.oauth.api.application.event.send_verify_email_event.SendVerifyEmailEvent;
import com.wiredpackage.oauth.api.application.models.identity.FaceInfoSummary;
import com.wiredpackage.oauth.api.application.models.sms_host.SmsInfo;
import com.wiredpackage.oauth.api.application.queries.identity.IdentityQueries;
import com.wiredpackage.oauth.api.application.queries.sms_host.SmsHostQueries;
import com.wiredpackage.oauth.api.application.services.CodeVerifyService;
import com.wiredpackage.oauth.api.application.services.CompanyService;
import com.wiredpackage.oauth.shared.constants.CodeVerifyType;
import com.wiredpackage.oauth.shared.constants.SmsHostType;
import com.wiredpackage.shared.application.exceptions.TaopassBadRequestException;
import com.wiredpackage.shared.application.exceptions.TaopassNotFoundException;
import com.wiredpackage.shared.application.exceptions.TaopassUnauthorizationException;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendCodeVerifyCommandHandler implements Command.Handler<SendCodeVerifyCommand, Long> {

    private final CompanyService companyService;
    private final IdentityQueries identityQueries;
    private final CodeVerifyService codeVerifyService;
    private final SmsHostQueries smsHostQueries;
    private final SmsServiceClient smsServiceClient;
    private final Pipeline pipeline;

    @Value("${mail.mail_form}")
    private String mailForm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long handle(SendCodeVerifyCommand command) {
        // find identity with faceId
        FaceInfoSummary faceInfo = identityQueries.findFaceInfoByFaceId(command.getFaceId())
            .orElseThrow(() -> new TaopassNotFoundException(MessageHelper.getMessage("face_id_not_found")));
        // gen verify code
        String codeVerify = codeVerifyService.registerCodeVerify(CodeVerifyType.AUTHENTICATION, faceInfo.getIdentityId(),
            command.getCodeChallenge(), command.getTypeVerify());

        if (command.getTypeVerify().equals(OAuthTwoFAType.EMAIL.name())) {
            if (StringUtils.isBlank(faceInfo.getEmail())) {
                log.info("----- Verify code email failed with face id {}", command.getFaceId());
                throw new TaopassUnauthorizationException(MessageHelper.getMessage("email_not_found"));
            }
            // Send mail verify token
            SendVerifyEmailEvent event = SendVerifyEmailEvent.builder()
                .mailFrom(mailForm)
                .mailTo(faceInfo.getEmail())
                .codeVerify(codeVerify)
                .build();
            pipeline.send(event);
        } else if (command.getTypeVerify().equals(OAuthTwoFAType.SMS.name())) {
            if (StringUtils.isBlank(faceInfo.getPhoneNumber())) {
                log.info("----- Verify code SMS failed with face id {}", command.getFaceId());
                throw new TaopassBadRequestException(
                    Map.of("error", MessageHelper.getMessage("phone_number_not_found")));
            }
            // Send sms
            SmsInfo smsInfo = smsHostQueries.getSmsInfo(SmsHostType.AUTHENTICATION.name());
            String message = String.format(smsInfo.getMessageFormat(), codeVerify, AuthConstants.CODE_VERIFY_EXPIRED_MINUTE);
            smsServiceClient.sendMessage(removeDash(faceInfo.getPhoneNumber()), message, smsInfo.getTokenSms(),
                companyService.createAccountCode(faceInfo.getCompanyId()));
        }
        return command.getFaceId();
    }

    private String removeDash(String phoneNumber) {
        return StringUtils.remove(phoneNumber, "-");
    }
}
