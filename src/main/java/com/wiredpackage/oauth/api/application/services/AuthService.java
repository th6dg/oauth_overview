package com.wiredpackage.oauth.api.application.services;

import com.wiredpackage.auth.shared.constants.AuthenticationObjItemField;
import com.wiredpackage.oauth.api.application.models.auth_scoring.AuthScoring;
import com.wiredpackage.oauth.api.application.models.member.MemberBasicInfo;
import com.wiredpackage.oauth.api.application.models.plan.PlanSummary;
import com.wiredpackage.oauth.api.application.models.service.ServiceSummary;
import com.wiredpackage.oauth.api.application.queries.authentication_obj.AuthenticationObjQueries;
import com.wiredpackage.oauth.api.application.queries.member.IMemberQueriesService;
import com.wiredpackage.oauth.api.application.queries.oauth.IOAuth2QueriesService;
import com.wiredpackage.oauth.api.application.queries.plan.IPlanQueriesService;
import com.wiredpackage.oauth.api.application.queries.schedule.ScheduleQueries;
import com.wiredpackage.oauth.api.application.queries.service.IServiceQueriesService;
import com.wiredpackage.oauth.api.dto.oauth.NeedSendApprovalNotify;
import com.wiredpackage.shared.application.dto.authentication_obj.AuthenticationObjDetails;
import com.wiredpackage.shared.application.exceptions.BadRequestException;
import com.wiredpackage.shared.application.exceptions.ForbiddenException;
import com.wiredpackage.shared.application.exceptions.NotFoundException;
import com.wiredpackage.shared.shared.helpers.MessageHelper;
import com.wiredpackage.shared.shared.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final IPlanQueriesService planQueriesService;
    private final IOAuth2QueriesService oAuth2QueriesService;
    private final IServiceQueriesService serviceQueriesService;
    private final IMemberQueriesService memberQueriesService;
    private final AuthenticationObjQueries authenticationObjQueries;
    private final ScheduleQueries scheduleQueries;
    private final PlanService planService;

    @Value("${oauth2.approval.expiration}")
    private Long oauth2ApprovalExpires;

    public boolean comparePassword(String password, String input) {
        return password.equals(input);
    }

    public void checkCertificationLimit(String serviceType, Long locationId, String cameraType) {
        ServiceSummary service = serviceQueriesService.findSummaryServiceByType(serviceType).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage("service_not_found")));
        PlanSummary plan = planQueriesService.findPlanSummaryByServiceIdAndLocationIdAndCameraType(
            service.getId(), locationId, cameraType, TimeUtils.currentDate()).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage("plan_not_found")));
        LocalDateTime dateNow = TimeUtils.now();
        LocalDateTime startDate = dateNow.withDayOfMonth(1);
        Long authenticationCount = oAuth2QueriesService.countOAuth2AuthenticationsByAuthenticationSettingIdAndTimeRange(
            plan.getAuthenticationCameraId(), startDate, dateNow);
        if (authenticationCount >= plan.getCertificationLimit()) {
            throw new BadRequestException(Map.of("plan", MessageHelper.getMessage("certification_limit_exceeded")));
        }
    }

    public void checkRegistrationLimit(Long planId) {
        PlanSummary plan = planQueriesService.findById(planId).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage("plan_not_found")));
        Long registrationCount = planQueriesService.countRegistrationByPlanId(plan.getId());
        if (registrationCount >= plan.getRegistrationLimit()) {
            throw new ForbiddenException(MessageHelper.getMessage("registration_limit_exceeded"));
        }
    }

    public boolean checkPlanValidation(String serviceType, Long locationId) {
        ServiceSummary service = serviceQueriesService.findSummaryServiceByType(serviceType).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage("service_not_found")));
        Optional<PlanSummary> plan = planQueriesService.findPlanSummaryByServiceIdAndLocationId(
            service.getId(), locationId, TimeUtils.currentDate());
        return plan.isPresent();
    }

    public boolean isApprovalExpired(LocalDateTime createdTime) {
        return TimeUtils.now().isAfter(createdTime.plusSeconds(oauth2ApprovalExpires));
    }

    public Optional<NeedSendApprovalNotify> needSendApprovalNotify(AuthScoring authScoring) {
        if (authScoring.getAuthorityId() == null) {
            return Optional.empty();
        }
        return Optional.of(new NeedSendApprovalNotify(authScoring.getAuthorityId(), authScoring.getThirdPartyRecognitionMethods()));

    }

    public boolean checkPermissionApproval(String serviceType, Long locationId, Long authorityId) {
        List<AuthenticationObjDetails> authenticationObjDetails =
            authenticationObjQueries.findAllItemsByServiceTypeAndLocationIdAndIsActiveAndAuthenticationObjItemType(
                serviceType,
                locationId,
                AuthenticationObjItemField.SCHEDULE_ID.name());
        List<Long> scheduleIds = authenticationObjDetails.stream().map(authenticationObjDetail -> Long.parseLong(
            authenticationObjDetail.getValue())).toList();
        if (!scheduleIds.isEmpty()) {
            List<Long> authorityIds = scheduleQueries.getAuthorityIdsByScheduleIdIn(scheduleIds);
            return authorityIds.contains(authorityId);
        }
        return false;
    }

    public Long getAuthorityId(Long identityId) {
        return memberQueriesService.findByIdentityId(identityId)
            .map(MemberBasicInfo::getAuthorityId)
            .orElse(null);
    }

}
