package com.topleader.topleader.feedback.api;


import com.topleader.topleader.feedback.entity.Question;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Accessors(chain = true)
public class FeedbackFormOptions {

    private static final String SCALE_KEY = "scale.";

    private List<OptionDto> options;

    private List<String> scales;

    public static FeedbackFormOptions of(List<Question> questions) {
        var options = questions.stream()
                .map(q -> new OptionDto(q.getKey(), QuestionType.PARAGRAPH))
                .collect(Collectors.toList());

        var scales = IntStream.range(1, 11)
                .mapToObj(i -> SCALE_KEY + i)
                .collect(Collectors.toList());

        return new FeedbackFormOptions()
                .setOptions(options)
                .setScales(scales);
    }

    public record OptionDto(String key, QuestionType type) {
    }
}
