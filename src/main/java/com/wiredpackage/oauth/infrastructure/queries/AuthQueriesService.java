package com.wiredpackage.oauth.infrastructure.queries;

import com.wiredpackage.oauth.api.application.models.auth.IdentityRole;
import com.wiredpackage.oauth.api.application.queries.auth.IAuthQueriesService;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.MemberJpaRepository;
import com.wiredpackage.oauth.infrastructure.jpa_repositories.RoleJpsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class AuthQueriesService implements IAuthQueriesService {
    private final MemberJpaRepository memberJpaRepository;
    private final RoleJpsRepository roleJpsRepository;

    @Override
    public Optional<IdentityRole> findById(Long id) {
        return roleJpsRepository.findRoleById(id);
    }

    @Override
    public List<IdentityRole> findIdentityRolesByIdentityId(Long identityId) {
        return memberJpaRepository.findIdentityRolesByIdentityId(identityId);
    }
}
