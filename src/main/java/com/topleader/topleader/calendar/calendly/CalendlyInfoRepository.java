package com.topleader.topleader.calendar.calendly;

import com.topleader.topleader.calendar.calendly.domain.CalendlyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendlyInfoRepository extends JpaRepository<CalendlyInfo, String> {
}
