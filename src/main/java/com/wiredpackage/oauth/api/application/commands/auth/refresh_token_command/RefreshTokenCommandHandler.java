package com.wiredpackage.oauth.api.application.commands.auth.refresh_token_command;

import an.awesome.pipelinr.Command;
import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.application.queries.auth.AuthQueries;
import com.wiredpackage.oauth.api.application.queries.identity.IIdentityQueriesService;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.dto.auth.LoginResDto;
import com.wiredpackage.oauth.domain.aggregate_models.oauth2_refresh_token_aggregate.OAuth2RefreshToken;
import com.wiredpackage.oauth.domain.repositories.IOAuth2RefreshTokenRepository;
import com.wiredpackage.oauth.infrastructure.services.TokenService;
import com.wiredpackage.shared.application.exceptions.UnauthorizationException;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class RefreshTokenCommandHandler implements
    Command.Handler<RefreshTokenCommand, LoginResDto> {
    private final IOAuth2RefreshTokenRepository oAuth2RefreshTokenRepository;
    private final AuthQueries authQueries;
    private final TokenService tokenService;
    private final IIdentityQueriesService identityQueriesService;
    private final AuthService authService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResDto handle(RefreshTokenCommand command) {
        OAuth2RefreshToken oAuth2RefreshToken = oAuth2RefreshTokenRepository.findByRefreshToken(command.getRefreshToken())
            .orElseThrow(() -> new UnauthorizationException(MessageHelper.getMessage("oauth2_refresh_token_not_found")));

        if (oAuth2RefreshToken.getExpiresAt().isBefore(TimeUtils.now())) {
            throw new UnauthorizationException(MessageHelper.getMessage("oauth2_refresh_token_expires"));
        }
        IdentityLogin identityLogin = identityQueriesService.findIdentityByIdentityId(oAuth2RefreshToken.getIdentityId())
            .orElseThrow(() -> new UnauthorizationException(MessageHelper.getMessage("identity_not_found")));


        List<String> roles = authQueries.getRoles(identityLogin.getId());
        if (roles.isEmpty()) {
            throw new UnauthorizationException(MessageHelper.getMessage("user_login_not_roles"));
        }

        Long authorityId = authService.getAuthorityId(identityLogin.getId());

        String accessToken = tokenService.signAccessToken(identityLogin.getId(), roles, identityLogin.getCompanyId(), authorityId);
        String refreshToken = tokenService.signRefreshToken();
        OAuth2RefreshToken newOAuth2RefreshToken = OAuth2RefreshToken.builder()
            .identityId(identityLogin.getId())
            .refreshToken(refreshToken)
            .expiresAt(tokenService.getRefreshTokenExpirationLocalDateTime())
            .build();
        newOAuth2RefreshToken = oAuth2RefreshTokenRepository.save(newOAuth2RefreshToken);
        log.info("----- RefreshTokenCommandHandler.handle: Identity {} refresh token updated {}",
            identityLogin.getId(), newOAuth2RefreshToken.id);
        return LoginResDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .identityId(identityLogin.getId())
            .build();
    }
}
