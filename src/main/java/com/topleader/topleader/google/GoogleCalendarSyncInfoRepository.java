/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface GoogleCalendarSyncInfoRepository  extends JpaRepository<GoogleCalendarSyncInfo, String> {
}
