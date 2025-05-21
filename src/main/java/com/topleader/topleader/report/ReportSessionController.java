package com.topleader.topleader.report;


import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;

import static com.topleader.topleader.exception.ErrorCodeConstants.NOT_PART_OF_COMPANY;

@RestController
@RequestMapping("/api/latest/report-sessions")
@RequiredArgsConstructor
class ReportSessionController {

   private final ReportSessionViewRepository repository;

   private final UserRepository userRepository;

    @PostMapping
    @Secured({"HR", "ADMIN"})
    public Page<ReportSessionDto> fetch(@PageableDefault(size = 25, sort = "username") Pageable pageable,
                                         @RequestBody ReportSessionFilter filter,
                                         @AuthenticationPrincipal UserDetails hrAuth) {

        var companyId = userRepository.findById(hrAuth.getUsername())
                .map(User::getCompanyId)
                .orElseThrow(() -> new ApiValidationException(NOT_PART_OF_COMPANY, "user", hrAuth.getUsername(), "User is not part of any company"));

         var all = repository.findAll(ReportSessionSpecification.withFilter(filter, companyId), pageable)
                .stream()
                .map(ReportSessionDto::from)
                .collect(Collectors.toMap(
                        ReportSessionDto::username,
                        dto -> dto,
                        (dto1, dto2) -> new ReportSessionDto(
                                dto1.username(),
                                dto1.firstName(),
                                dto1.lastName(),
                                dto1.attended() + dto2.attended(),
                                dto1.booked() + dto2.booked()
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(ReportSessionDto::username)) // apply sorting
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        var pageContent = all.subList(start, end);

        return new PageImpl<>(pageContent, pageable, all.size());
    }

    public record ReportSessionDto(String username, String firstName, String lastName, int attended, int booked) {

        public static ReportSessionDto from(ReportSessionView reportSession) {
            return new ReportSessionDto(reportSession.getUsername(),
                    reportSession.getFirstName(),
                    reportSession.getLastName(),
                    ScheduledSession.Status.COMPLETED == reportSession.getStatus() ? 1 : 0,
                    ScheduledSession.Status.COMPLETED != reportSession.getStatus() &&  reportSession.getStatus() != null ? 1 : 0 );
        }

    }
}


