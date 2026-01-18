package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.Recipient;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends CrudRepository<Recipient, Long> {

    Optional<Recipient> findByFormIdAndRecipientAndToken(long formId, String recipient, String token);

    List<Recipient> findByFormId(long formId);

    void deleteByFormId(long formId);

    @Modifying
    @Query("DELETE FROM fb_recipient WHERE form_id = :formId AND id NOT IN (:keepIds)")
    void deleteByFormIdAndIdNotIn(long formId, List<Long> keepIds);
}
