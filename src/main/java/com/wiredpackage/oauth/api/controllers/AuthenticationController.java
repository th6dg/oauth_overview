package com.wiredpackage.oauth.api.controllers;

import com.wiredpackage.oauth.api.application.queries.authentication.AuthenticationQueries;
import com.wiredpackage.oauth.api.application.queries.plan.IPlanQueriesService;
import com.wiredpackage.oauth.api.application.services.AuthService;
import com.wiredpackage.oauth.api.dto.authentication.AuthenticationConfigResDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Slf4j
@AllArgsConstructor
@Tag(name = "authentications")
@RequestMapping("authentications")
@RestController
public class AuthenticationController {
    private final AuthenticationQueries authenticationQueries;
    private final AuthService authService;
    private final IPlanQueriesService planQueriesService;

    @GetMapping
    public AuthenticationConfigResDto getAuthenticationConfig(@RequestParam("clientId") String clientId,
                                                              @RequestParam("locationId") Long locationId,
                                                              @RequestParam("codeChallenge") String codeChallenge,
                                                              @RequestHeader(value = "deviceIdentity", required = false) String deviceIdentity) {
        return authenticationQueries.getAuthenticationConfig(clientId, locationId, codeChallenge, deviceIdentity);
    }

    @GetMapping("stream-camera")
    public AuthenticationConfigResDto getAuthenticationConfigStreamCamera(@RequestParam("cameraId") Long cameraId,
                                                                          @RequestParam("locationId") Long locationId,
                                                                          @RequestParam("codeChallenge") String codeChallenge,
                                                                          @RequestHeader(value = "deviceIdentity", required = false) String deviceIdentity) {
        return authenticationQueries.getAuthenticationConfigStreamCamera(cameraId, locationId, codeChallenge, deviceIdentity);
    }

    @GetMapping("check-plan-expires")
    public boolean checkPlanExpires(@RequestParam("serviceType") String serviceType,
                                    @RequestParam("locationId") Long locationId) {
        return authService.checkPlanValidation(serviceType, locationId);
    }

    @GetMapping("check-certification-limit")
    public void checkCertificationLimit(@RequestParam("serviceType") String serviceType,
                                        @RequestParam("locationId") Long locationId,
                                        @RequestParam("cameraType") String cameraType) {
        authService.checkCertificationLimit(serviceType, locationId, cameraType);
    }

    @GetMapping("ai-repo")
    public Long getAIRepoIdByRepositoryId(@RequestParam("repoId") Long repositoryId) {
        return planQueriesService.findAIRepoIdByRepositoryId(repositoryId);
    }
}
