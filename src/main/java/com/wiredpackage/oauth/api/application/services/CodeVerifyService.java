package com.wiredpackage.oauth.api.application.services;

import com.wiredpackage.oauth.domain.aggregate_models.code_verify_aggregate.CodeVerify;
import com.wiredpackage.oauth.domain.repositories.ICodeVerifyRepository;
import com.wiredpackage.oauth.shared.constants.CodeVerifyStatus;
import com.wiredpackage.oauth.shared.constants.CodeVerifyType;
import com.wiredpackage.shared.application.exceptions.InnerServerErrorException;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.wiredpackage.auth.shared.constants.AuthConstants.CODE_VERIFY_EXPIRED_MINUTE;
import static com.wiredpackage.auth.shared.constants.AuthConstants.CODE_VERIFY_LENGTH;

@Slf4j
@RequiredArgsConstructor
@Service
public class CodeVerifyService {

    private final ICodeVerifyRepository codeVerifyRepository;


    @Transactional(rollbackFor = Exception.class)
    public String registerCodeVerify(CodeVerifyType type, Long identityId, String codeChallenge, String type2FA) {
        LocalDateTime now = TimeUtils.now();
        long timestamp = TimeUtils.getInstant(now).toEpochMilli();
        String code = "";
        try {
            code = generateCodeVerify(timestamp, identityId);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (StringUtils.isBlank(code) || code.trim().length() != CODE_VERIFY_LENGTH) {
            log.error("Cannot generate code verify for identityId :{} ", identityId);
            throw new InnerServerErrorException("code_verify_cannot_generate");
        }
        CodeVerify codeVerify = CodeVerify.builder()
            .identityId(identityId)
            .code(code)
            .type(type.name())
            .status(CodeVerifyStatus.NEW.name())
            .codeChallenge(codeChallenge)
            .type2fa(type2FA)
            .expiredAt(now.plusMinutes(CODE_VERIFY_EXPIRED_MINUTE))
            .build();
        codeVerify = codeVerifyRepository.save(codeVerify);
        return codeVerify.getCode();
    }

    @Transactional(rollbackFor = Exception.class)
    public String verifyCode(String code, CodeVerifyType type, Long identityId, String codeChallenge) {
        Optional<CodeVerify> codeVerifyOptional =
            codeVerifyRepository.findByCodeVerify(identityId, code, type.name(), CodeVerifyStatus.NEW.name(), TimeUtils.now(), codeChallenge);
        if (codeVerifyOptional.isEmpty()) {
            return MessageHelper.getMessage("pin_code_invalid");
        }
        CodeVerify codeVerify = codeVerifyOptional.get();
        codeVerify.upStatus(CodeVerifyStatus.VERIFIED.name());
        codeVerifyRepository.save(codeVerify);
        return "";
    }

    private static String generateCodeVerify(long timestamp, long identityId) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = (String.format("%d%d", timestamp, identityId)).getBytes(StandardCharsets.UTF_8);
        md.update(bytes);
        byte[] hash = md.digest();
        String hashString = DatatypeConverter.printHexBinary(hash);
        String code = hashString.replaceAll("[^0-9]", "");
        return code.substring(code.length() - CODE_VERIFY_LENGTH);
    }
}
