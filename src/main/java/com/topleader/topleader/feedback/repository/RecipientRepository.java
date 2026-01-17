package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.Recipient;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends CrudRepository<Recipient, Long> {

    Optional<Recipient> findByFormIdAndRecipientAndToken(long formId, String recipient, String token);

    List<Recipient> findByFormId(long formId);

    void deleteByFormId(long formId);
}
