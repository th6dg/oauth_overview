package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.api.application.models.auth.OAuth2WaitingApprovalDetail;
import com.wiredpackage.oauth.api.application.models.waiting_approval.WaitingApprovalBasicInfo;
import com.wiredpackage.oauth.infrastructure.entities.OAuth2WaitingApprovalEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2WaitingApprovalJpaRepository extends BaseJpaRepository<OAuth2WaitingApprovalEntity, Long> {
    Optional<OAuth2WaitingApprovalEntity> findByItemId(String itemId);

    @Query(value = "SELECT awa.id AS id, awa.item_id AS itemId, awa.oauth2_grant_id AS oauth2GrantId, " +
        "   awa.identity_id AS identityId, awa.approved AS approved, awa.created_at AS createdAt " +
        "FROM oauth2_waiting_approval awa " +
        "WHERE awa.item_id = :waitingApprovalItemId ", nativeQuery = true)
    Optional<OAuth2WaitingApprovalDetail> findOAuth2WaitingApprovalDetailByWaitingApprovalItemId(
        @Param("waitingApprovalItemId") String waitingApprovalItemId);

    @Query(value = "SELECT owa.id AS id, owa.item_id AS itemId, s.type AS service, og.state AS state, owa.approved AS approved, " +
        "   ol.access_time AS accessTime, ol.log_image_object_key AS logImageObjectKey, i.id AS identityId, " +
        "   m.first_name AS identityFirstName, m.last_name AS identityLastName " +
        "FROM oauth2_waiting_approval owa " +
        "INNER JOIN oauth2_logs ol ON ol.id = owa.oauth2_log_id " +
        "INNER JOIN oauth2_grants og ON ol.oauth2_grant_id = og.id " +
        "INNER JOIN services s ON s.id = og.service_id " +
        "INNER JOIN identities i ON i.id = owa.identity_id " +
        "INNER JOIN members m ON m.identity_id = i.id " +
        "WHERE s.type = :service AND og.location_id = :locationId AND owa.created_at > :expiredBefore " +
        "AND owa.approved = false AND owa.valid = true ", nativeQuery = true)
    List<WaitingApprovalBasicInfo> findAllValidByServiceAndLocationIdAndExpiredBefore(@Param("service") String service,
                                                                                      @Param("locationId") Long locationId,
                                                                                      @Param("expiredBefore") LocalDateTime expiredBefore);

    Boolean existsByOauth2GrantId(Long id);

    Boolean existsByOauth2LogId(Long id);
}
