package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Optional<Recipient> findByFormIdAndRecipientAndToken(long formId, String recipient,  String token);
}
