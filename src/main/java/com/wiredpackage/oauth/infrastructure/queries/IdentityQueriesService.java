package com.wiredpackage.oauth.infrastructure.queries;

import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.application.models.identity.FaceInfoSummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentitySummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentityTagInfo;
import com.wiredpackage.oauth.api.application.queries.identity.IIdentityQueriesService;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.IdentityJpaRepository;
import com.wiredpackage.shared.shared.constants.RoleEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class IdentityQueriesService implements IIdentityQueriesService {
    private final IdentityJpaRepository identityJpaRepository;

    @Override
    public Optional<IdentitySummary> findIdentityByFaceIdAndCompanyId(Long faceId, Long companyId) {
        return identityJpaRepository.findIdentityByFaceIdAndCompanyId(faceId, companyId);
    }

    @Override
    public Optional<IdentityLogin> findIdentityByCompanyIdAndLoginId(Long companyId, String loginId) {
        if (companyId == null) {
            return identityJpaRepository.findIdentityByLoginId(loginId);
        }
        return identityJpaRepository.findIdentityByCompanyIdAndLoginId(companyId, loginId);
    }

    @Override
    public Optional<IdentityLogin> findIdentityByIdentityId(Long identityId) {
        return identityJpaRepository.findIdentityLoginByIdentityId(identityId);
    }

    @Override
    public Optional<FaceInfoSummary> findFaceInfoByFaceId(Long faceId) {
        return identityJpaRepository.findFaceInfoByFaceId(faceId);
    }

    @Override
    public Optional<IdentityLogin> findIdentityManagerByIdentityUser(Long identityId, Long companyId) {
        return identityJpaRepository.findIdentityManagerByIdentityUser(identityId, companyId,
            RoleEnum.MANAGER_CONTROL.name(), RoleEnum.MANAGER_EDITOR.name(), RoleEnum.MANAGER_VIEWER.name());
    }

    @Override
    public Set<FaceInfoSummary> findFaceInfosInServiceWithClientId(Set<Long> faceIds, Long serviceId, Long locationId) {
        return identityJpaRepository.findFaceInfosInServiceWithClientId(faceIds, serviceId, locationId);
    }

    @Override
    public Optional<IdentitySummary> findIdentityByEmailAndCompanyId(String email, Long companyId) {
        return identityJpaRepository.findIdentityByEmailAndCompanyId(email, companyId);
    }

    @Override
    public Long countIdentityByQrInvitationCodeIdExceptFaceId(Long qrInvitationCodeId, Long faceId) {
        return identityJpaRepository.countAllByQrInvitationCodeIdAndFaceIdNot(qrInvitationCodeId, faceId);
    }

    @Override
    public Optional<IdentitySummary> findIdentitySummaryByFaceId(Long faceId) {
        return identityJpaRepository.findIdentitySummaryByFaceId(faceId);
    }

    @Override
    public List<IdentityTagInfo> findTagsByIdentityIds(Collection<Long> identityIds) {
        return identityJpaRepository.findTagsByIdentityIds(identityIds);
    }
}
