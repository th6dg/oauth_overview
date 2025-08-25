package com.wiredpackage.oauth.api.application.queries.authentication_obj;

import com.wiredpackage.shared.application.dto.authentication_obj.AuthenticationObjDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthenticationObjQueries {
    private final IAuthenticationObjQueriesService authenticationObjQueriesService;

    public List<AuthenticationObjDetails> findAllItemsByObjId(Long id){
        return authenticationObjQueriesService.findAllItemsByObjId(id);
    }

    public List<AuthenticationObjDetails> findAllItemsByServiceTypeAndLocationIdAndIsActiveAndAuthenticationObjItemType(
        String serviceType,
        Long locationId,
        String authenticationObjItemType) {
        return authenticationObjQueriesService.findAllItemsByServiceTypeAndLocationIdAndIsActiveAndAuthenticationObjItemType(
            serviceType,
            locationId,
            authenticationObjItemType);
    }
}
