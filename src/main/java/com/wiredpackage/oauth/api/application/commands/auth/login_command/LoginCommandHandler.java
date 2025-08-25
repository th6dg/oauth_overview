package com.wiredpackage.oauth.api.application.commands.auth.login_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.oauth.api.application.queries.auth.AuthQueries;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.dto.auth.LoginResDto;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_refresh_token_aggregate.OAuth2RefreshToken;
import com.wiredpackage.oauth.domain.repositories.IOAuth2AuthenticationRepository;
import com.wiredpackage.oauth.domain.repositories.IOAuth2RefreshTokenRepository;
import com.wiredpackage.oauth.infrastructure.services.TokenService;
import com.wiredpackage.shared.application.exceptions.UnauthorizationException;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class LoginCommandHandler implements Command.Handler<LoginCommand, LoginResDto> {
    private final IOAuth2RefreshTokenRepository oAuth2RefreshTokenRepository;
    private final IOAuth2AuthenticationRepository oAuth2AuthenticationRepository;
    private final AuthQueries authQueries;
    private final TokenService tokenService;
    private final AuthService authService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResDto handle(LoginCommand command) {
        if (command.getOauth2AuthenticationId() != null) {
            oAuth2AuthenticationRepository.findById(command.getOauth2AuthenticationId())
                .orElseThrow(() -> new UnauthorizationException(MessageHelper.getMessage("oauth2_authentication_not_found")));
        }

        List<String> roles = authQueries.getRoles(command.getIdentityLogin().getId());
        if (roles.isEmpty()) {
            throw new UnauthorizationException(MessageHelper.getMessage("user_login_not_roles"));
        }
        Long authorityId = authService.getAuthorityId(command.getIdentityLogin().getId());

        String accessToken = tokenService.signAccessToken(command.getIdentityLogin().getId(), roles,
            command.getIdentityLogin().getCompanyId(), authorityId);

        String refreshToken = tokenService.signRefreshToken();
        OAuth2RefreshToken newOAuth2RefreshToken = OAuth2RefreshToken.builder()
            .identityId(command.getIdentityLogin().getId())
            .refreshToken(refreshToken)
            .expiresAt(tokenService.getRefreshTokenExpirationLocalDateTime())
            .build();
        oAuth2RefreshTokenRepository.save(newOAuth2RefreshToken);
        log.info("----- LoginCommandHandler.handle: Identity {} login successfully",
            command.getIdentityLogin().getId());
        String[] gpsInfo = splitGps(command.getGps());
        return LoginResDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .identityId(command.getIdentityLogin().getId())
            .gpsLat(gpsInfo[0])
            .gpsLong(gpsInfo[1])
            .build();
    }

    private String[] splitGps(String gps) {
        try {
            return gps.split(",", 2);
        } catch (Exception e) {
            return new String[]{null, null};
        }
    }
}
