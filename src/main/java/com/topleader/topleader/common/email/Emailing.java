package com.topleader.topleader.common.email;

import com.topleader.topleader.common.calendar.ical.ICalEvent;

public interface Emailing {

    void sendEmail(String to, String subject, String body);

    void sendEmail(String from, String to, String subject, String body);

    void sendEmail(String to, String subject, String body, ICalEvent event);
}
