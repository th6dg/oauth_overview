package com.wiredpackage.oauth.api.application.queries.auth;

import com.wiredpackage.oauth.api.application.models.auth.IdentityRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AuthQueries {
    private final IAuthQueriesService authQueriesService;

    public List<String> getRoles(Long identityId) {
        return authQueriesService.findIdentityRolesByIdentityId(identityId).stream().map(IdentityRole::getType).toList();
    }
}
