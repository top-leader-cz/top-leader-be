package com.topleader.topleader.program.manager;

import com.topleader.topleader.program.CompanyResolver;
import com.topleader.topleader.program.participant.ProgramParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/latest/manager/programs")
@Secured({"MANAGER"})
@RequiredArgsConstructor
public class ManagerProgramController {

    private final ProgramParticipantRepository participantRepository;
    private final CompanyResolver companyResolver;

    @GetMapping
    public List<ManagerProgramView> listManagedPrograms(@AuthenticationPrincipal UserDetails user) {
        var companyId = companyResolver.resolveCompanyId(user.getUsername());
        var rows = participantRepository.findManagedParticipants(user.getUsername(), companyId);
        return rows.stream()
                .collect(Collectors.groupingBy(
                        ManagerParticipantRow::programId,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(e -> new ManagerProgramView(
                        e.getKey(),
                        e.getValue().getFirst().programName(),
                        e.getValue().stream()
                                .map(r -> new ManagerProgramView.ManagerParticipantView(
                                        r.username(),
                                        r.firstName(),
                                        r.lastName(),
                                        r.enrollmentStatus(),
                                        r.attendanceCount(),
                                        r.practiceCompletionRate()))
                                .toList()))
                .toList();
    }
}
