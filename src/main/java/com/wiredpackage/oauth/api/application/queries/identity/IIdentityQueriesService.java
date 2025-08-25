package com.wiredpackage.oauth.api.application.queries.identity;

import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.application.models.identity.FaceInfoSummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentitySummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentityTagInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IIdentityQueriesService {
    Optional<IdentitySummary> findIdentityByFaceIdAndCompanyId(Long faceId, Long companyId);

    Optional<IdentityLogin> findIdentityByCompanyIdAndLoginId(Long companyId, String loginId);

    Optional<IdentityLogin> findIdentityByIdentityId(Long identityId);

    Optional<FaceInfoSummary> findFaceInfoByFaceId(Long faceId);

    Optional<IdentityLogin> findIdentityManagerByIdentityUser(Long identityId, Long companyId);

    Set<FaceInfoSummary> findFaceInfosInServiceWithClientId(Set<Long> faceIds, Long serviceId, Long locationId);

    Optional<IdentitySummary> findIdentityByEmailAndCompanyId(String email, Long companyId);

    Long countIdentityByQrInvitationCodeIdExceptFaceId(Long qrInvitationCodeId, Long faceId);

    Optional<IdentitySummary> findIdentitySummaryByFaceId(Long faceId);

    List<IdentityTagInfo> findTagsByIdentityIds(Collection<Long> identityIds);
}
