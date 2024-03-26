package com.topleader.topleader.ai;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AiPromptService {

    private final AiPromptRepository aiPromptRepository;

    public String getPrompt(AiPrompt.PromptType promptType) {
        List<AiPrompt> all = aiPromptRepository.findAll();

        return aiPromptRepository.findById(promptType)
                .orElseThrow(() -> new RuntimeException("Prompt not found"))
                .getValue();
    }
}

