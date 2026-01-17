package com.topleader.topleader.common.email;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateServiceTest {

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService();
    }

    @Test
    void getMessage_shouldReplaceVariablesInICalTemplate() {
        var params = Map.<String, Object>of(
            "method", "REQUEST",
            "startDate", "20240115T100000Z",
            "endDate", "20240115T110000Z",
            "eventId", "test-123",
            "eventName", "Test Event",
            "organizer", "org@test.com",
            "coach", "coach@test.com",
            "coachName", "Coach Name",
            "client", "client@test.com",
            "clientName", "Client Name"
        );

        var result = templateService.getMessage(params, "templates/ical/event.ics");

        assertThat(result)
            .contains("METHOD:REQUEST")
            .contains("DTSTART:20240115T100000Z")
            .contains("DTEND:20240115T110000Z")
            .contains("UID:test-123")
            .contains("SUMMARY:Test Event")
            .contains("ORGANIZER;CN=org@test.com:mailto:org@test.com")
            .contains("CN=Coach Name")
            .contains("mailto:coach@test.com")
            .contains("CN=Client Name")
            .contains("mailto:client@test.com");
    }

    @Test
    void getMessage_shouldReplaceVariablesInPrivateEventTemplate() {
        var params = Map.<String, Object>of(
            "method", "CANCEL",
            "startDate", "20240220T143000Z",
            "endDate", "20240220T153000Z",
            "eventId", "private-456",
            "eventName", "Private Event",
            "organizer", "org@test.com",
            "client", "client@test.com",
            "clientName", "Client Name"
        );

        var result = templateService.getMessage(params, "templates/ical/private-event.ics");

        assertThat(result)
            .contains("METHOD:CANCEL")
            .contains("UID:private-456")
            .contains("CN=Client Name")
            .contains("mailto:client@test.com")
            .doesNotContain("coach");
    }

    @Test
    void getMessage_shouldHandleMissingVariableAsEmpty() {
        var params = Map.<String, Object>of(
            "method", "REQUEST",
            "startDate", "20240115T100000Z",
            "endDate", "20240115T110000Z",
            "eventId", "test-123",
            "organizer", "org@test.com",
            "coach", "coach@test.com",
            "coachName", "Coach",
            "client", "client@test.com",
            "clientName", "Client"
            // eventName is missing
        );

        var result = templateService.getMessage(params, "templates/ical/event.ics");

        assertThat(result)
            .contains("SUMMARY:")
            .contains("DESCRIPTION:");
    }

    @Test
    void getMessage_shouldThrowExceptionForMissingTemplate() {
        var params = Map.<String, Object>of("key", "value");

        assertThatThrownBy(() -> templateService.getMessage(params, "templates/nonexistent.ics"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template not found");
    }
}
