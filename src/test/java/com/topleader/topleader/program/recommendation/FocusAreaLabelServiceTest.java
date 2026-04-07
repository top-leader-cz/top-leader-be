package com.topleader.topleader.program.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FocusAreaLabelServiceTest {

    private final FocusAreaLabelService service = new FocusAreaLabelService(new ObjectMapper());

    @Test
    void labelFor_returnsEnglishLabel() {
        assertThat(service.labelFor("fa.giving-feedback", "en")).isEqualTo("Giving feedback");
    }

    @Test
    void labelFor_returnsCzechLabel() {
        assertThat(service.labelFor("fa.giving-feedback", "cs")).isEqualTo("Poskytování zpětné vazby");
    }

    @Test
    void labelFor_returnsGermanLabel() {
        assertThat(service.labelFor("fa.delegation", "de")).isEqualTo("Delegieren");
    }

    @Test
    void labelFor_fallsBackToEnglish_whenLanguageMissing() {
        assertThat(service.labelFor("fa.giving-feedback", "xx")).isEqualTo("Giving feedback");
    }

    @Test
    void labelFor_returnsKey_whenFocusAreaUnknown() {
        assertThat(service.labelFor("fa.unknown", "en")).isEqualTo("fa.unknown");
    }

    @Test
    void englishLabel_returnsEnglish() {
        assertThat(service.englishLabel("fa.strategic-thinking")).isEqualTo("Strategic thinking");
    }

    @Test
    void englishLabel_returnsKey_whenFocusAreaUnknown() {
        assertThat(service.englishLabel("fa.unknown")).isEqualTo("fa.unknown");
    }
}
