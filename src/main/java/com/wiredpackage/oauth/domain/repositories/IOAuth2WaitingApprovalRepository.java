package com.wiredpackage.oauth.domain.repositories;

import com.wiredpackage.oauth.domain.aggregate_models.oauth2_waiting_approval.OAuth2WaitingApproval;
import com.wiredpackage.shared.domain.seed_work.IRepository;

import java.util.Optional;

public interface IOAuth2WaitingApprovalRepository extends IRepository<OAuth2WaitingApproval> {
    Optional<OAuth2WaitingApproval> findByItemId(String itemId);

    Boolean existsByOauth2GrandId(Long id);

    Boolean existByOauth2LogId(Long id);

    void flush();
}
