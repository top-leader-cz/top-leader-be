package com.topleader.topleader.util.common;

import com.topleader.topleader.feedback.api.FeedbackData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {


//    String prompt = "Based on the user''s top 5 strengths: {0}, selected values: {1}, chosen area for development: {2}, long-term goal: {3}, and short-term goals: {4}, here are your tailored steps to achieve each short-term goal:\n" +
//            "For each short-term goal:\n" +
//            "Identify the most critical action that aligns with one of your top strengths: {0} and values: {1}.\n" +
//            "Define a clear, actionable task that contributes directly to the short-term goal, ensuring it is Specific, Measurable, Achievable, Relevant, and Time-bound (SMART).\n" +
//            "Establish a mini-deadline within the next 1-2 weeks to accomplish this task.\n" +
//            "Determine a small reward for completing the task, linking it to your values: {1} s for added motivation.\n" +
//            "Reflect briefly on how accomplishing this task will bring you closer to your long-term goal: {3}.\n" +
//            "Repeat these steps for each short-term goal, maintaining focus and alignment with user''s strengths and values. Keep sentences concise and in the first person to engage directly with the user. All guidance should be in {5} language.'";
//    String getPrompt = "fsdf {0} " +
//            "\n ss {1} " +
//            "\n sss {2} " +
//            "znova {0}";
//
//    String prompt2 = "Based on the user's top 5 strengths: {0}, selected values: {1}";
//


    @Test
    void dataTest() {
//        String format1 = MessageFormat.format(getPrompt, "for", "jedna" , "dva");
//        String format = MessageFormat.format(prompt, "dfsd","fds", "s", "k", "l", "d");
        var v = LocalDateTime.now().isBefore(LocalDateTime.parse("2024-01-06T00:00").plusDays(1));
        Assertions.assertThat(JsonUtils.toJson(new FeedbackData().setValidTo(LocalDateTime.now()))).isNotEmpty();
    }
}