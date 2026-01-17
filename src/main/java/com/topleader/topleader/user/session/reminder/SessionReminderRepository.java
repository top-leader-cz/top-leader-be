package com.topleader.topleader.user.session.reminder;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SessionReminderRepository extends CrudRepository<SessionReminderView, String>, PagingAndSortingRepository<SessionReminderView, String> {
}
