package com.topleader.topleader.program;

import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.user.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.NOT_PART_OF_COMPANY;

@Component
@RequiredArgsConstructor
public class CompanyResolver {

    private final UserDetailService userDetailService;

    public Long resolveCompanyId(String username) {
        var user = userDetailService.getUser(username).orElseThrow(NotFoundException::new);
        return Optional.ofNullable(user.getCompanyId())
                .orElseThrow(() -> new ApiValidationException(NOT_PART_OF_COMPANY, "user", username, "User is not part of any company"));
    }
}
