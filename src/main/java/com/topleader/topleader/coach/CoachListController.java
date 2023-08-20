/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.util.page.PageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasExperienceBetween;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasExperienceFrom;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasExperienceTo;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasFieldsInList;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasLanguagesInList;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasRateInSet;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.nameStartsWith;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coaches")
public class CoachListController {

    private final CoachRepository coachRepository;

    @PostMapping
    public Page<CoachListDto> findCoaches(@RequestBody @Valid FilterRequest request) {
        return findCoaches(request.toSpecification(), request.page().toPageable())
            .map(CoachListDto::from);
    }

    private Page<Coach> findCoaches(List<Specification<Coach>> filter, Pageable page) {
        return filter.isEmpty() ?
            coachRepository.findAll(page) :
            coachRepository.findAll(Specification.allOf(filter), page);

    }

    public record CoachListDto(
        String username,
        String firstName,
        String lastName,
        String email,

        byte[] photo,

        String bio,

        Set<String> languages,

        Set<String> fields,

        Integer experience,

        String rate
    ) {
        public static CoachListDto from(Coach c) {
            return new CoachListDto(
                c.getUsername(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getPhoto(),
                c.getBio(),
                c.getLanguages(),
                c.getFields(),
                toExperience(c.getExperienceSince()),
                c.getRate()
            );
        }

        private static Integer toExperience(LocalDate experienceSince) {
            return Optional.ofNullable(experienceSince)
                .map(LocalDate::getYear)
                .map(year -> LocalDate.now().getYear() - year)
                .orElse(null);
        }
    }

    public record FilterRequest(
        @NotNull
        PageDto page,
        List<String> languages,
        List<String> fields,
        Integer experienceFrom,
        Integer experienceTo,
        List<String> prices,
        String name
    ) {

        public List<Specification<Coach>> toSpecification() {

            final var result = new ArrayList<Specification<Coach>>();

            Optional.ofNullable(languages())
                .ifPresent(languages -> result.add(hasLanguagesInList(languages)));

            Optional.ofNullable(fields())
                .ifPresent(fields -> result.add(hasFieldsInList(fields)));

            if (experienceFrom() != null && experienceTo() != null) {
                result.add(hasExperienceBetween(toDate(experienceFrom()), toDate(experienceTo())));
            }

            if (experienceFrom() != null && experienceTo() == null) {
                result.add(hasExperienceFrom(toDate(experienceFrom())));
            }

            if (experienceFrom() == null && experienceTo() != null) {
                result.add(hasExperienceTo(toDate(experienceTo())));
            }

            Optional.ofNullable(prices())
                .ifPresent(prices -> result.add(hasRateInSet(prices)));

            Optional.ofNullable(name())
                .ifPresent(name -> result.add(nameStartsWith(name)));

            return result;
        }

        private static LocalDate toDate(Integer i) {
            return LocalDate.now()
                .withMonth(1)
                .withDayOfMonth(1)
                .minusYears(i);
        }

    }
}
