package com.topleader.topleader.user.session.feedback;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionFeedbackRepository extends JpaRepository<SessionFeedback, Long> {

    Optional<SessionFeedback> findBySessionIdAndUsername(Long sessionId, String username);
}
