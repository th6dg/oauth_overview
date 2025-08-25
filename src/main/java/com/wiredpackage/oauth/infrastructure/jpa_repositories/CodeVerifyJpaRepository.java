package com.wiredpackage.oauth.infrastructure.jpa_repositories;

import com.wiredpackage.oauth.infrastructure.entities.CodeVerifyEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodeVerifyJpaRepository extends BaseJpaRepository<CodeVerifyEntity, Long>{

    @Query(value = "SELECT * FROM code_verify cv WHERE cv.identity_id = :identityId " +
        "AND cv.code = :code AND cv.type = :type AND cv.status = :status AND cv.code_challenge = :codeChallenge AND :currentTime <= cv.expired_at ", nativeQuery = true)
    Optional<CodeVerifyEntity> findByCodeVerify(@Param("identityId")Long identityId, @Param("code")String code,
                                                @Param("type")String type, @Param("status")String status,
                                                @Param("currentTime") LocalDateTime currentTime,
                                                @Param("codeChallenge") String codeChallenge);

    Optional<CodeVerifyEntity> findTopByIdentityIdAndCodeChallenge(Long identityId, String codeChallenge);
}
