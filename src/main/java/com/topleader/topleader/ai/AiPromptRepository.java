package com.topleader.topleader.ai;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptRepository extends JpaRepository<AiPrompt, AiPrompt.PromptType> {
}
