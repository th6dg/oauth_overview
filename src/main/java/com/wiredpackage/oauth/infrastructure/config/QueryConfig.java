package com.wiredpackage.oauth.infrastructure.config;

import com.wiredpackage.oauth.api.application.queries.auth.IAuthQueriesService;
import com.wiredpackage.oauth.api.application.queries.auth_scoring.IAuthScoringQueriesService;
import com.wiredpackage.oauth.api.application.queries.authentication.IAuthenticationSettingQueriesService;
import com.wiredpackage.oauth.api.application.queries.identity.IIdentityQueriesService;
import com.wiredpackage.oauth.api.application.queries.location.ILocationQueriesService;
import com.wiredpackage.oauth.api.application.queries.member.IMemberQueriesService;
import com.wiredpackage.oauth.api.application.queries.oauth.IOAuth2QueriesService;
import com.wiredpackage.oauth.api.application.queries.plan.IPlanQueriesService;
import com.wiredpackage.oauth.api.application.queries.schedule.IScheduleQueriesService;
import com.wiredpackage.oauth.api.application.queries.service.IServiceQueriesService;
import com.wiredpackage.oauth.api.application.queries.waiting_approval.IWaitingApprovalQueriesService;
import com.wiredpackage.oauth.infrastructure.queries.*;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AllArgsConstructor
public class QueryConfig {
    private final AuthQueriesService authQueriesService;
    private final AuthenticationSettingQueriesService authenticationSettingQueriesService;
    private final IdentityQueriesService identityQueriesService;
    private final OAuth2QueriesService oAuth2QueriesService;
    private final ServiceQueriesService serviceQueriesService;
    private final PlanQueriesService planQueriesService;
    private final ScheduleQueriesService scheduleQueriesService;
    private final AuthScoringQueriesService authScoringQueriesService;
    private final WaitingApprovalQueriesService waitingApprovalQueriesService;
    private final MemberQueriesService memberQueriesService;
    private final LocationQueriesService locationQueriesService;

    @Bean
    public IAuthQueriesService getAuthQueriesService() {
        return authQueriesService;
    }

    @Bean
    public IAuthenticationSettingQueriesService getAuthenticationSettingQueriesService() {
        return authenticationSettingQueriesService;
    }

    @Bean
    public IIdentityQueriesService getIdentityQueriesService() {
        return identityQueriesService;
    }

    @Bean
    public IOAuth2QueriesService getOAuth2QueriesService() {
        return oAuth2QueriesService;
    }

    @Bean
    public IServiceQueriesService getServiceQueriesService() {
        return serviceQueriesService;
    }

    @Bean
    public IPlanQueriesService getPlanQueriesService() {
        return planQueriesService;
    }

    @Bean
    public IScheduleQueriesService getScheduleQueriesService() {
        return scheduleQueriesService;
    }

    @Bean
    public IAuthScoringQueriesService getAuthScoringQueriesService() {
        return authScoringQueriesService;
    }

    @Bean
    public IWaitingApprovalQueriesService getWaitingApprovalQueriesService() {
        return waitingApprovalQueriesService;
    }

    @Primary
    @Bean
    public IMemberQueriesService getMemberQueriesService() {
        return memberQueriesService;
    }

    @Bean
    public ILocationQueriesService getLocationQueriesService() {
        return locationQueriesService;
    }
}
