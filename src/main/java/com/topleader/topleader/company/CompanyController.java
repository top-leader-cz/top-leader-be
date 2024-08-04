/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.company;

import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.exception.ErrorCodeConstants.ALREADY_EXISTING;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/companies")
@AllArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    @GetMapping
    @Secured("ADMIN")
    public List<CompanyDto> listCompanies() {
        return companyRepository.findAll().stream()
            .map(CompanyDto::from)
            .toList();
    }

    @PostMapping
    @Secured("ADMIN")
    public CompanyDto createCompany(@RequestBody CreateCompanyRequest request) {

        final var existingCompany = companyRepository.findByName(request.name());

        if (existingCompany.isPresent()) {
            throw new ApiValidationException(ALREADY_EXISTING, "name", request.name(), "Company with this name already exists");
        }

        return CompanyDto
            .from(
                companyRepository.save(
                    new Company().setName(request.name())
                )
            );

    }

    @PostMapping("/{company}")
    @Secured("ADMIN")
    public CompanyDto updateCompany(
        @PathVariable String company,
        @RequestBody UpdateCompanyRequest request
    ) {

        final var existingCompany = companyRepository.findByName(company)
            .orElseThrow(NotFoundException::new);

        return CompanyDto
            .from(
                companyRepository.save(
                    existingCompany.setDefaultMaxCoachRate(request.defaultMaxCoachRate())
                )
            );

    }

    public record CreateCompanyRequest(String name) {
    }

    public record UpdateCompanyRequest(String defaultMaxCoachRate) {
    }

    public record CompanyDto(Long id, String name, String defaultMaxCoachRate) {
        public static CompanyDto from(Company c) {
            return new CompanyDto(c.getId(), c.getName(), c.getDefaultMaxCoachRate());
        }
    }
}
