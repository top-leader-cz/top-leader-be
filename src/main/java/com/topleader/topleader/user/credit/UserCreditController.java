package com.topleader.topleader.user.credit;

import com.topleader.topleader.common.email.EmailService;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.hr.domain.CreditRequestDto;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.common.util.transaction.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/latest/user-credits")
@AllArgsConstructor
public class UserCreditController {

    private final UserDetailService userDetailService;

    private final TransactionService transactionService;

    private final EmailService emailService;

    @Secured({"USER"})
    @GetMapping
    public RequestCreditsDto getCredits(@AuthenticationPrincipal UserDetails user) {
        return RequestCreditsDto.from(userDetailService.getUser(user.getUsername()).orElseThrow(NotFoundException::new));
    }

    @Secured({"USER"})
    @PostMapping
    public RequestCreditsDto requestCredits(@AuthenticationPrincipal UserDetails user, @RequestBody CreditRequestDto request) {
        var username = user.getUsername();
        return transactionService.execute(() -> userDetailService.getUser(user.getUsername())
                .map(u -> u.setRequestedCredit(request.credit()))
                .map(u -> {
                    var saved = userDetailService.save(u);
                    var body = String.format("User: %s Amount: %s Timestamp: %s", user.getUsername(),  request.credit(), LocalDateTime.now());
                    emailService.sendEmail("info@topleader.io", "Credits requested in the TopLeader platform", body);
                    return RequestCreditsDto.from(saved);
                })
                .orElseThrow(NotFoundException::new)
        );
    }
}
