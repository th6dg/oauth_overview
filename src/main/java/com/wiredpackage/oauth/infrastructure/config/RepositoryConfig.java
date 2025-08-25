package com.wiredpackage.oauth.infrastructure.config;

import com.wiredpackage.oauth.domain.repositories.*;
import com.wiredpackage.oauth.infrastructure.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@AllArgsConstructor
@Configuration
public class RepositoryConfig {
    private final OAuth2AuthenticationRepository oAuth2AuthenticationRepository;
    private final OAuth2CodeRepository oAuth2CodeRepository;
    private final OAuth2GrantRepository oAuth2GrantRepository;
    private final OAuth2RefreshTokenRepository oAuth2RefreshTokenRepository;
    private final OAuth2WaitingApprovalRepository oAuth2WaitingApprovalRepository;

    @Primary
    @Bean
    public IOAuth2AuthenticationRepository getOAuth2AuthenticationRepository() {
        return oAuth2AuthenticationRepository;
    }

    @Primary
    @Bean
    public IOAuth2CodeRepository getOAuth2CodeRepository() {
        return oAuth2CodeRepository;
    }

    @Primary
    @Bean
    public IOAuth2GrantRepository getOAuth2GrantRepository() {
        return oAuth2GrantRepository;
    }

    @Primary
    @Bean
    public IOAuth2RefreshTokenRepository getOAuth2RefreshTokenRepository() {
        return oAuth2RefreshTokenRepository;
    }

    @Primary
    @Bean
    public IOAuth2WaitingApprovalRepository getOAuth2WaitingApprovalRepository() {
        return oAuth2WaitingApprovalRepository;
    }
}
