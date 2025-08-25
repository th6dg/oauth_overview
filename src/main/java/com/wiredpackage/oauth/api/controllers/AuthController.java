package com.wiredpackage.oauth.api.controllers;

import an.awesome.pipelinr.Pipeline;
import com.wiredpackage.oauth.api.application.commands.auth.generate_external_token_command.GenerateExternalTokenCommand;
import com.wiredpackage.oauth.api.application.commands.auth.login_command.LoginCommand;
import com.wiredpackage.oauth.api.application.commands.auth.refresh_token_command.RefreshTokenCommand;
import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.application.queries.auth.AuthQueries;
import com.wiredpackage.oauth.api.application.queries.identity.IdentityQueries;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.dto.auth.LoginReqDto;
import com.wiredpackage.oauth.api.dto.auth.LoginResDto;
import com.wiredpackage.oauth.api.dto.auth.RefreshReqDto;
import com.wiredpackage.shared.application.dto.oauth_service.GenerateExternalTokenDto;
import com.wiredpackage.shared.application.exceptions.UnauthorizationException;
import com.wiredpackage.shared.shared.constants.RoleEnum;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Slf4j
@AllArgsConstructor
@Tag(name = "auth")
@RequestMapping("auth")
@RestController
public class AuthController {
    private final Pipeline pipeline;
    private final IdentityQueries identityQueries;
    private final AuthService authService;

    private final AuthQueries authQueries;

    @PostMapping("login")
    public LoginResDto login(@RequestBody @Valid LoginReqDto request) {
        IdentityLogin identityLogin =
            identityQueries.findIdentityByCompanyIdAndLoginId(request.getAccountId(), request.getLoginId())
                .orElseThrow(() -> {
                        log.error("Identity not found {} {}", request.getAccountId(), request.getLoginId());
                        return new UnauthorizationException(MessageHelper.getMessage("identity_not_found"));
                    }
                );
        if (!authService.comparePassword(identityLogin.getPassword(), request.getPassword())) {
            throw new UnauthorizationException(MessageHelper.getMessage("identity_not_found"));
        }
        List<String> roles = authQueries.getRoles(identityLogin.getId());
        if (roles.isEmpty()) {
            throw new UnauthorizationException(MessageHelper.getMessage("user_login_not_roles"));
        }
        if (request.getAccountId() != null && (RoleEnum.isAdmin(roles) || RoleEnum.isCompany(roles)) ||
            request.getAccountId() == null && !RoleEnum.isAdmin(roles) && !RoleEnum.isCompany(roles) && !RoleEnum.isManager(roles)) {
            throw new UnauthorizationException(MessageHelper.getMessage("identity_not_found"));
        }
        LoginCommand command = LoginCommand.builder()
            .identityLogin(identityLogin)
            .build();
        return pipeline.send(command);
    }

    @PostMapping("refresh")
    public LoginResDto refresh(@RequestBody @Valid RefreshReqDto refreshReqDto) {
        return pipeline.send(RefreshTokenCommand.builder()
            .refreshToken(refreshReqDto.getRefreshToken())
            .build());
    }

    @PostMapping("generate-external-token")
    public String generateExternalToken(@RequestBody @Valid GenerateExternalTokenDto request) {
        GenerateExternalTokenCommand command = GenerateExternalTokenCommand.builder()
            .companyId(request.getCompanyId())
            .managementTokenId(request.getManagementTokenId())
            .build();
        return pipeline.send(command);
    }

    @PostMapping("login-suite")
    public LoginResDto loginSuite(@RequestBody @Valid LoginReqDto request) {
        IdentityLogin identityLogin =
            identityQueries.findIdentityByCompanyIdAndLoginId(request.getAccountId(), request.getLoginId())
                .orElseThrow(() -> {
                        log.error("Identity not found {} {}", request.getAccountId(), request.getLoginId());
                        return new UnauthorizationException(MessageHelper.getMessage("identity_not_found"));
                    }
                );
        if (!authService.comparePassword(identityLogin.getPassword(), request.getPassword())) {
            throw new UnauthorizationException(MessageHelper.getMessage("identity_not_found"));
        }
        List<String> roles = authQueries.getRoles(identityLogin.getId());
        if (roles.isEmpty()) {
            throw new UnauthorizationException(MessageHelper.getMessage("user_login_not_roles"));
        }
        if (request.getAccountId() != null && RoleEnum.isAdmin(roles) ||
            request.getAccountId() == null && !RoleEnum.isAdmin(roles) && !RoleEnum.isCompany(roles) && !RoleEnum.isManager(roles)) {
            throw new UnauthorizationException(MessageHelper.getMessage("identity_not_found"));
        }
        LoginCommand command = LoginCommand.builder()
            .identityLogin(identityLogin)
            .build();
        return pipeline.send(command);
    }
}
