package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.api.application.models.auth.IdentityLogin;
import com.wiredpackage.oauth.api.application.models.identity.FaceInfoSummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentitySummary;
import com.wiredpackage.oauth.api.application.models.identity.IdentityTagInfo;
import com.wiredpackage.oauth.infrastructure.entities.IdentityEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IdentityJpaRepository extends BaseJpaRepository<IdentityEntity, Long> {
    @Query(value = "SELECT i.id AS id, i.company_id AS companyId, i.pin_code AS pinCode, i.face_id AS faceId, " +
        "m.phone_number AS phoneNumber, m.email AS email, i.is_registering AS isRegistering " +
        "FROM identities i " +
        "LEFT JOIN members AS m ON m.identity_id = i.id " +
        "WHERE i.face_id = :faceId AND i.company_id = :companyId AND i.is_deleted = FALSE ", nativeQuery = true)
    Optional<IdentitySummary> findIdentityByFaceIdAndCompanyId(@Param("faceId") Long faceId, @Param("companyId") Long companyId);

    @Query(value = "SELECT i.id, i.login_id AS loginId, i.password AS password, i.face_id AS faceId, " +
        " i.company_id AS companyId, i.is_registering AS isRegistering " +
        "FROM identities i " +
        "WHERE i.company_id = :companyId AND i.login_id = :loginId " +
        "AND i.is_deleted = FALSE ",
        nativeQuery = true)
    Optional<IdentityLogin> findIdentityByCompanyIdAndLoginId(@Param("companyId") Long companyId, @Param("loginId") String loginId);

    @Query(value = "SELECT i.id, i.login_id AS loginId, i.password AS password, i.face_id AS faceId, " +
        " i.company_id AS companyId, i.is_registering AS isRegistering " +
        "FROM identities i " +
        "WHERE i.login_id = :loginId " +
        "AND i.is_deleted = FALSE ",
        nativeQuery = true)
    Optional<IdentityLogin> findIdentityByLoginId(@Param("loginId") String loginId);

    @Query(value = "SELECT i.id, i.login_id AS loginId, i.password AS password, i.face_id AS faceId, " +
        " i.company_id AS companyId, i.is_registering AS isRegistering " +
        "FROM identities i " +
        "WHERE i.id = :identityId AND i.is_deleted = FALSE ",
        nativeQuery = true)
    Optional<IdentityLogin> findIdentityLoginByIdentityId(@Param("identityId") Long identityId);

    @Query(value = "SELECT i.id AS identityId, i.company_id as companyId, m.last_name AS lastName, m.first_name AS firstName, m.id AS memberId, " +
        "m.email AS email, m.phone_number AS phoneNumber, i.is_registering AS isRegistering, g.name AS gender, r.name AS race, " +
        "IF (m.date_of_birth IS NOT NULL AND m.date_of_birth < NOW(), YEAR(NOW()) - YEAR(m.date_of_birth), NULL) as age " +
        "FROM members m " +
        "INNER JOIN identities i on m.identity_id = i.id " +
        "LEFT JOIN genders g on m.gender_id = g.id " +
        "LEFT JOIN racials r on m.racial_id = r.id " +
        "WHERE i.face_id = :faceId AND i.is_deleted = false ", nativeQuery = true)
    Optional<FaceInfoSummary> findFaceInfoByFaceId(@Param("faceId") Long faceId);

    @Query(value = "SELECT ms.identityId AS id, ms.loginId AS loginId, ms.password AS password, ms.companyId AS companyId, " +
        "ms.memberId AS memberId, ms.faceId AS faceId, c.id AS ownerCompanyId, i.is_registering AS isRegistering " +
        "FROM members m " +
        "INNER JOIN identities i on m.identity_id = i.id " +
        "LEFT JOIN companies c ON c.id = i.company_id " +
        "RIGHT JOIN (SELECT m1.first_name AS firstName, m1.last_name AS lastName, " +
        "m1.email AS email, i1.face_id AS faceId, i1.company_id AS companyId, m1.identity_id AS identityId, " +
        "m1.id as memberId, i1.login_id AS loginId, i1.password AS password " +
        "FROM members m1  " +
        "INNER JOIN identities i1 ON m1.identity_id = i1.id  " +
        "INNER JOIN identity_role ir ON ir.identity_id = i1.id " +
        "INNER JOIN roles r ON r.id = ir.role_id " +
        "WHERE m1.is_deleted = false AND i1.is_deleted = FALSE AND i1.company_id = :companyId " +
        "AND r.`type` IN (:managerControl, :managerEditor, :managerViewer) ) AS ms " +
        "ON m.email = ms.email " +
        "WHERE m.is_deleted = false AND i.is_deleted = false AND i.id = :identityId AND i.company_id = :companyId",
        nativeQuery = true)
    Optional<IdentityLogin> findIdentityManagerByIdentityUser(@Param("identityId") Long identityId,
                                                              @Param("companyId") Long companyId,
                                                              @Param("managerControl") String managerControl,
                                                              @Param("managerEditor") String managerEditor,
                                                              @Param("managerViewer") String managerViewer);

    @Query(value = "SELECT i.id AS identityId, i.company_id as companyId, m.last_name AS lastName, m.first_name AS firstName, m.id AS memberId, " +
        "m.email AS email, m.phone_number AS phoneNumber, i.is_registering AS isRegistering, g.name AS gender, r.name AS race, i.face_id AS faceId, " +
        "IF (m.date_of_birth IS NOT NULL AND m.date_of_birth < NOW(), YEAR(NOW()) - YEAR(m.date_of_birth), NULL) as age " +
        "FROM members m " +
        "INNER JOIN identities i ON m.identity_id = i.id " +
        "LEFT JOIN genders g ON m.gender_id = g.id " +
        "LEFT JOIN racials r ON m.racial_id = r.id " +
        "INNER JOIN identity_service `is` ON i.id = `is`.identity_id " +
        "INNER JOIN locations l ON `is`.location_id = l.id " +
        "INNER JOIN services s ON `is`.service_id = s.id " +
        "WHERE i.face_id IN (:faceId) AND i.is_deleted = false " +
        "AND l.id = :locationId " +
        "AND s.id = :serviceId " +
        "GROUP BY i.id, m.id ",
        nativeQuery = true)
    Set<FaceInfoSummary> findFaceInfosInServiceWithClientId(@Param("faceId") Set<Long> faceIds,
                                                            @Param("serviceId") Long serviceId,
                                                            @Param("locationId") Long locationId);

    @Query(value = "SELECT i.id AS id, i.company_id AS companyId, i.pin_code AS pinCode, i.face_id AS faceId, m.phone_number AS phoneNumber, m.email AS email " +
        "FROM identities i " +
        "JOIN members AS m ON m.identity_id = i.id " +
        "WHERE m.email = :email AND i.company_id = :companyId AND i.is_deleted = FALSE", nativeQuery = true)
    Optional<IdentitySummary> findIdentityByEmailAndCompanyId(@Param("email") String email,
                                                              @Param("companyId") Long companyId);

    Long countAllByQrInvitationCodeIdAndFaceIdNot(Long qrInvitationCodeId, Long faceId);


    @Query(value = "SELECT i.id AS id, i.company_id AS companyId, i.pin_code AS pinCode, i.face_id AS faceId, " +
        "m.phone_number AS phoneNumber, m.email AS email, i.is_registering AS isRegistering " +
        "FROM identities i " +
        "LEFT JOIN members AS m ON m.identity_id = i.id " +
        "WHERE i.face_id = :faceId AND i.is_deleted = FALSE ", nativeQuery = true)
    Optional<IdentitySummary> findIdentitySummaryByFaceId(@Param("faceId") Long faceId);

    @Query(value = "SELECT i.id AS identityId, t.id AS tagId, t.name AS tagName " +
        "FROM identities i " +
        "JOIN members m ON i.id = m.identity_id " +
        "JOIN member_tag mt ON m.id = mt.member_id " +
        "JOIN tags t ON mt.tag_id = t.id " +
        "WHERE i.id IN (:identityIds) ", nativeQuery = true)
    List<IdentityTagInfo> findTagsByIdentityIds(@Param("identityIds") Collection<Long> identityIds);
}
