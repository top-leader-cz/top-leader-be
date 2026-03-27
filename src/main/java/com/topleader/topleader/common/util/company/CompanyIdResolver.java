package com.topleader.topleader.common.util.company;

import java.util.Optional;

public interface CompanyIdResolver {
    Optional<Long> getCompanyId(String username);
}
