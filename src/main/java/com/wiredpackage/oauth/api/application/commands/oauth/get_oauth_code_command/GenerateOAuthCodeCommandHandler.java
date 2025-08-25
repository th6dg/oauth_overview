package com.wiredpackage.oauth.api.application.commands.oauth.get_oauth_code_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.auth.infrastructure.services.OAuthService;
import com.wiredpackage.oauth.api.application.services.AuthenticationService;
import com.wiredpackage.oauth.api.application.services.PlanService;
import com.wiredpackage.oauth.api.dto.authentication.OAuth2AuthenticationRedisDto;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_authentication_aggregate.OAuth2Authentication;
import com.wiredpackage.oauth.domain.repositories.IOAuth2AuthenticationRepository;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2AuthenticationEntity;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2AuthenticationJpaRepository;
import com.wiredpackage.shared.shared.constants.DefaultAccountAuthSettings;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static com.wiredpackage.auth.shared.constants.AuthConstants.REDIS_OAUTH2_AUTHENTICATION_PREFIX;
import static com.wiredpackage.shared.shared.utils.TimeUtils.YYYYMMDDHHmmss;

@Slf4j
@AllArgsConstructor
@Component
public class GenerateOAuthCodeCommandHandler implements Command.Handler<GenerateOAuthCodeCommand, String> {
    private final IOAuth2AuthenticationRepository oAuth2AuthenticationRepository;
    private final OAuthService oAuthService;
    private final RedisTemplate<String, OAuth2AuthenticationRedisDto> redisTemplateOauth2Authentication;
    private final AuthenticationService authenticationService;
    private final PlanService planService;
    private final OAuth2AuthenticationJpaRepository oAuth2AuthenticationJpaRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handle(GenerateOAuthCodeCommand command) {
        String redisKey = REDIS_OAUTH2_AUTHENTICATION_PREFIX + command.getFaceId() + '_' + command.getAuthenticationSettingId();

        //if oAuth2Authentication already exists, not update authenticationCode
        OAuth2Authentication oAuth2Authentication = OAuth2Authentication.builder()
            .identityId(command.getIdentityId())
            .authenticationSettingId(command.getAuthenticationSettingId())
            .oauth2GrantId(command.getOAuthGrantId())
            .expiresAt(oAuthService.getAuthorizationCodeExpiration())
            .authenticationCode(oAuthService.generateAuthorizationCode())
            .build();

        Optional<OAuth2AuthenticationEntity> existingGrantOauthAuthenticationEntity = oAuth2AuthenticationJpaRepository
            .findFirstByOauth2GrantId(command.getOAuthGrantId());
        if (!Objects.equals(DefaultAccountAuthSettings.DEFAULT_AUTHENTICATION_SETTING_ID, command.getAuthenticationSettingId()) &&
            existingGrantOauthAuthenticationEntity.isEmpty()) {
            planService.checkReachCertificationLimitToSendMail(command.getAuthenticationSettingId(), command.getLocationId());
        }
        oAuth2Authentication = oAuth2AuthenticationRepository.saveOrUpdate(oAuth2Authentication, existingGrantOauthAuthenticationEntity);
        OAuth2AuthenticationRedisDto redisDto = OAuth2AuthenticationRedisDto.builder()
            .identityId(oAuth2Authentication.getIdentityId())
            .authenticationSettingId(oAuth2Authentication.getAuthenticationSettingId())
            .oauth2GrantId(oAuth2Authentication.getOauth2GrantId())
            .expiresAt(TimeUtils.parseString(oAuth2Authentication.getExpiresAt(), YYYYMMDDHHmmss))
            .authenticationCode(oAuth2Authentication.getAuthenticationCode())
            .authenticationTimeLast(TimeUtils.parseString(TimeUtils.now(), YYYYMMDDHHmmss))
            .build();
        redisTemplateOauth2Authentication.opsForValue().set(redisKey, redisDto,
            authenticationService.getRedisCameraTimeout());
        return oAuth2Authentication.getAuthenticationCode();
    }
}
