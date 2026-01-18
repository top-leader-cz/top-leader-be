package com.topleader.topleader.common.ai;

import com.topleader.topleader.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiPromptService {

    private final AiPromptRepository aiPromptRepository;

    public String getPrompt(AiPrompt.PromptType promptType) {
        return aiPromptRepository.findById(promptType)
                .orElseThrow(NotFoundException::new)
                .getValue();
    }

    public Prompt prompt(AiPrompt.PromptType promptType, Map<String, Object> params) {
        return new PromptTemplate(getPrompt(promptType)).create(params);
    }
}

