package com.topleader.topleader.common.calendar.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("coach_availability_settings")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
public class CoachAvailabilitySettings  {

    @Id
    private Long id;

    private String coach;

    private CalendarSyncInfo.SyncType type;

    private String resource;

    boolean active;

}
