package com.topleader.topleader.program.template;

import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/latest/hr/program-templates")
@RequiredArgsConstructor
@Secured({"HR", "ADMIN"})
public class ProgramTemplateController {

    private final ProgramTemplateRepository templateRepository;
    private final UserDetailService userDetailService;

    @GetMapping
    public List<TemplateDto> list(@AuthenticationPrincipal UserDetails user) {
        var companyId = resolveCompanyId(user.getUsername());
        return templateRepository.findAvailable(companyId).stream()
                .map(TemplateDto::from)
                .toList();
    }

    @GetMapping("/{templateId}")
    public TemplateDto detail(@AuthenticationPrincipal UserDetails user, @PathVariable Long templateId) {
        var companyId = resolveCompanyId(user.getUsername());
        return templateRepository.findById(templateId)
                .filter(t -> t.isActive() && (t.getCompanyId() == null || t.getCompanyId().equals(companyId)))
                .map(TemplateDto::from)
                .orElseThrow(NotFoundException::new);
    }

    private Long resolveCompanyId(String username) {
        return userDetailService.getUser(username)
                .map(User::getCompanyId)
                .orElseThrow(NotFoundException::new);
    }

    public record TemplateDto(
            Long id,
            String name,
            String description,
            String goal,
            String targetGroup,
            Integer durationDays,
            boolean custom,
            Set<String> focusAreas
    ) {
        static TemplateDto from(ProgramTemplate t) {
            return new TemplateDto(
                    t.getId(),
                    t.getName(),
                    t.getDescription(),
                    t.getGoal(),
                    t.getTargetGroup(),
                    t.getDurationDays(),
                    t.getCompanyId() != null,
                    t.getFocusAreas()
            );
        }
    }
}
