package com.wiredpackage.oauth.api.application.queries.waiting_approval;

import com.wiredpackage.oauth.api.application.models.waiting_approval.WaitingApprovalBasicInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface IWaitingApprovalQueriesService {
    List<WaitingApprovalBasicInfo> findAllValidByServiceAndLocationIdAndExpiredBefore(
        String service, Long locationId, LocalDateTime expiredBefore);
}
