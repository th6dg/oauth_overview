package com.wiredpackage.oauth.api.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import static com.wiredpackage.shared.shared.constants.Constants.REFRESH_TOKEN_MAX_LENGTH;

@Getter
@Setter
public class RefreshReqDto {
    @NotEmpty
    @NotNull
    @Length(max = REFRESH_TOKEN_MAX_LENGTH)
    private String refreshToken;
}
