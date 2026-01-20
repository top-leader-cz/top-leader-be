package com.topleader.topleader.user.session.feedback;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SessionFeedbackRepository extends CrudRepository<SessionFeedback, Long>, PagingAndSortingRepository<SessionFeedback, Long> {
}
