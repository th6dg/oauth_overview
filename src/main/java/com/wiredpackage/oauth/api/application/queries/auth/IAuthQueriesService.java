package com.wiredpackage.oauth.api.application.queries.auth;

import com.wiredpackage.oauth.api.application.models.auth.IdentityRole;

import java.util.List;
import java.util.Optional;

public interface IAuthQueriesService {
    Optional<IdentityRole> findById(Long id);

    List<IdentityRole> findIdentityRolesByIdentityId(Long identityId);
}
