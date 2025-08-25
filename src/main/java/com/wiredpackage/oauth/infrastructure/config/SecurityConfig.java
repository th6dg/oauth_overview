package com.wiredpackage.oauth.infrastructure.config;

import com.wiredpackage.shared.application.exceptions.ExceptionHandlerFilter;
import com.wiredpackage.shared.application.security.AuthorizatonFilter;
import com.wiredpackage.shared.infrastructure.services.SharedTokenService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final SharedTokenService sharedTokenService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .cors().and()
            .authorizeHttpRequests((authz) -> authz.requestMatchers(
                "/v3/api-docs/**",
                "/swagger",
                "/swagger/**",
                "/swagger-ui/**").authenticated())
            .httpBasic()
            .and()
            .authorizeHttpRequests()
            .requestMatchers("**").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterAfter(new AuthorizatonFilter(sharedTokenService), BasicAuthenticationFilter.class)
            .addFilterBefore(new ExceptionHandlerFilter(), BasicAuthenticationFilter.class)
            .logout();
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern(CorsConfiguration.ALL);
        configuration.addAllowedMethod(CorsConfiguration.ALL);
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService swaggerUserService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserDetails userDetails = User
            .withUsername("swagger")
            .password(encoder.encode("nu8BUxoJqgLGyco"))
            .roles("basicAuth")
            .build();
        return new InMemoryUserDetailsManager(userDetails);
    }
}
