package com.topleader.topleader.program.enrollment;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.program.participant.ProgramParticipant;
import com.topleader.topleader.program.participant.ProgramParticipantRepository;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.token.Token;
import com.topleader.topleader.user.token.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgramEnrollmentEmailIT extends IntegrationTest {

    @Autowired
    private ProgramParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Test
    @Sql("/sql/enrollment/enrollment-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_sendsEnrollmentEmailToExistingUser() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/1/launch"))
                .andExpect(status().isOk());

        var messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        var msg = messages[0];
        assertThat(GreenMailUtil.getAddressList(msg.getAllRecipients())).isEqualTo("user1@test.cz");
        assertThat(msg.getSubject()).isEqualTo("You've been enrolled in Launch Test Program");

        var body = GreenMailUtil.getBody(msg);
        assertThat(body)
                .contains("Jan")
                .contains("Launch Test Program")
                .contains("Improve leadership")
                .contains("http://app-test-url/#/program-enrollment/1");

        var participant = participantRepository.findByProgramIdAndUsername(1L, "user1@test.cz").orElseThrow();
        assertThat(participant.getEnrollmentEmailSentAt()).isNotNull();
        assertThat(participant.getEnrollmentEmailScheduledAt()).isNotNull();
        assertThat(participant.isNewUser()).isFalse();
    }

    @Test
    @Sql("/sql/enrollment/enrollment-new-user-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_createsNewUserAndSendsEnrollmentEmail() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/1/launch"))
                .andExpect(status().isOk());

        var messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        var msg = messages[0];
        assertThat(GreenMailUtil.getAddressList(msg.getAllRecipients())).isEqualTo("newuser@test.cz");
        assertThat(msg.getSubject()).isEqualTo("You've been invited to New User Program");

        var body = GreenMailUtil.getBody(msg);
        assertThat(body)
                .contains("there")
                .contains("New User Program")
                .contains("create-password");

        var newUser = userRepository.findByUsername("newuser@test.cz").orElseThrow();
        assertThat(newUser.getStatus()).isEqualTo(com.topleader.topleader.user.User.Status.AUTHORIZED);
        assertThat(newUser.getCompanyId()).isEqualTo(100L);

        var participant = participantRepository.findByProgramIdAndUsername(1L, "newuser@test.cz").orElseThrow();
        assertThat(participant.isNewUser()).isTrue();
        assertThat(participant.getEnrollmentEmailSentAt()).isNotNull();

        var token = tokenService.findByTokenAndType(
                extractToken(body), Token.Type.SET_PASSWORD);
        assertThat(token).isPresent();
    }

    @Test
    @Sql("/sql/enrollment/enrollment-future-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_schedulesEmailForFutureStartDate() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/1/launch"))
                .andExpect(status().isOk());

        assertThat(greenMail.getReceivedMessages()).isEmpty();

        var participant = participantRepository.findByProgramIdAndUsername(1L, "user1@test.cz").orElseThrow();
        assertThat(participant.getEnrollmentEmailScheduledAt()).isNotNull();
        assertThat(participant.getEnrollmentEmailSentAt()).isNull();
    }

    @Test
    @Sql("/sql/enrollment/enrollment-resend-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void resendEnrollmentEmail_sendsForInvitedParticipant() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/1/participants/user1@test.cz/resend-enrollment"))
                .andExpect(status().isOk());

        var messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getSubject()).isEqualTo("You've been enrolled in Resend Test Program");

        var participant = participantRepository.findByProgramIdAndUsername(1L, "user1@test.cz").orElseThrow();
        assertThat(participant.getEnrollmentEmailSentAt()).isNotNull();
    }

    @Test
    @Sql("/sql/enrollment/enrollment-resend-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void resendEnrollmentEmail_failsForActiveParticipant() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/1/participants/user2@test.cz/resend-enrollment"))
                .andExpect(status().isUnprocessableEntity());

        assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    @Test
    @Sql("/sql/enrollment/enrollment-job-test.sql")
    @WithMockUser(username = "job-trigger", authorities = "JOB")
    void jobEndpoint_processesScheduledEmails() throws Exception {
        mvc.perform(post("/api/protected/jobs/enrollment-emails"))
                .andExpect(status().isOk());

        var messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(GreenMailUtil.getAddressList(messages[0].getAllRecipients())).isEqualTo("user1@test.cz");

        var participant = participantRepository.findByProgramIdAndUsername(1L, "user1@test.cz").orElseThrow();
        assertThat(participant.getEnrollmentEmailSentAt()).isNotNull();
    }

    private String extractToken(String body) {
        var marker = "token=3D";
        var idx = body.indexOf(marker);
        if (idx == -1) {
            marker = "token=";
            idx = body.indexOf(marker);
        }
        if (idx == -1) {
            return "";
        }
        var start = idx + marker.length();
        var end = body.indexOf("&", start);
        if (end == -1) {
            end = body.indexOf("\"", start);
        }
        return end == -1 ? body.substring(start) : body.substring(start, end);
    }
}
