package com.topleader.topleader.user.session.reminder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionReminderRepository extends JpaRepository<SessionReminderView, String> {
}
