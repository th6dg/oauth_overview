package com.wiredpackage.oauth.api.application.queries.authentication_obj;

import com.wiredpackage.oauth.api.application.models.authentication_obj.AuthenticationObjOverview;
import com.wiredpackage.shared.application.dto.authentication_obj.AuthenticationObjDetails;

import java.util.List;

public interface IAuthenticationObjQueriesService {

    List<AuthenticationObjOverview> findAllAuthenticationObjOverviewByServiceIdAndLocationId(Long serviceId,
                                                                                             Long locationId,
                                                                                             boolean isActive,
                                                                                             boolean isLocation);

    List<AuthenticationObjDetails> findAllItemsByObjId(Long id);

    List<AuthenticationObjDetails> findAllItemsByServiceTypeAndLocationIdAndIsActiveAndAuthenticationObjItemType(String serviceType,
                                                                                                                 Long locationId,
                                                                                                                 String authenticationObjItemType);
}
