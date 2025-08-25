package com.wiredpackage.oauth.infrastructure.queries;

import com.wiredpackage.oauth.api.application.models.waiting_approval.WaitingApprovalBasicInfo;
import com.wiredpackage.oauth.api.application.queries.waiting_approval.IWaitingApprovalQueriesService;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.OAuth2WaitingApprovalJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class WaitingApprovalQueriesService implements IWaitingApprovalQueriesService {
    private final OAuth2WaitingApprovalJpaRepository oAuth2WaitingApprovalJpaRepository;

    @Override
    public List<WaitingApprovalBasicInfo> findAllValidByServiceAndLocationIdAndExpiredBefore(
        String service, Long locationId, LocalDateTime expiredBefore) {
        return oAuth2WaitingApprovalJpaRepository.findAllValidByServiceAndLocationIdAndExpiredBefore(
            service, locationId, expiredBefore);
    }
}
