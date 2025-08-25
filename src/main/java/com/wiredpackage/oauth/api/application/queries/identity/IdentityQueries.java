package com.wiredpackage.oauth.api.application.queries.identity;

import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.application.models.identity.FaceInfoSummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentitySummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentityTagInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class IdentityQueries {
    private final IIdentityQueriesService identityQueriesService;

    public Optional<IdentitySummary> findIdentityByFaceIdAndCompanyId(Long faceId, Long companyId) {
        return identityQueriesService.findIdentityByFaceIdAndCompanyId(faceId, companyId);
    }

    public Optional<IdentityLogin> findIdentityByCompanyIdAndLoginId(Long companyId, String loginId) {
        return identityQueriesService.findIdentityByCompanyIdAndLoginId(companyId, loginId);
    }

    public Optional<IdentityLogin> findIdentityByIdentityId(Long identityId) {
        return identityQueriesService.findIdentityByIdentityId(identityId);
    }

    public Optional<FaceInfoSummary> findFaceInfoByFaceId(Long faceId) {
        return identityQueriesService.findFaceInfoByFaceId(faceId);
    }

    public Optional<IdentityLogin> findIdentityManagerByIdentityUser(Long identityId, Long companyId) {
        return identityQueriesService.findIdentityManagerByIdentityUser(identityId, companyId);
    }

    public Set<FaceInfoSummary> findFaceInfosInServiceWithClientId(Set<Long> faceIds, Long serviceId, Long locationId) {
        return identityQueriesService.findFaceInfosInServiceWithClientId(faceIds, serviceId, locationId);
    }

    public Optional<IdentitySummary> findIdentityByEmailAndCompanyId(String email, Long companyId) {
        return identityQueriesService.findIdentityByEmailAndCompanyId(email, companyId);
    }

    public Optional<IdentitySummary> findIdentitySummaryByFaceId(Long faceId) {
        return identityQueriesService.findIdentitySummaryByFaceId(faceId);
    }

    public List<IdentityTagInfo> findTagsByIdentityIds(Collection<Long> identityIds) {
        return identityQueriesService.findTagsByIdentityIds(identityIds);
    }
}
