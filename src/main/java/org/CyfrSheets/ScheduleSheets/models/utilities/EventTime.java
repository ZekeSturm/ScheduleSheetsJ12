package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;

import javax.persistence.*;
import java.util.Calendar;

@Entity
public class EventTime {

    @Id
    int id;

    Calendar startTime;
    Calendar endTime;

    @OneToOne(mappedBy = "time")
    @MapsId
    BaseEvent parentEvent;

    // Store user times here for planning events

    boolean hasEndTime;

    public EventTime(Calendar startTime, Calendar endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        hasEndTime = true;
    }

    public EventTime(Calendar startTime) {
        this.startTime = startTime;
        hasEndTime = false;
    }

    public boolean hasEnd() { return hasEndTime; }

    public boolean multiDay() {
        // TODO - return if startTime day and endTime day are not equal

        // Error stifling - remove when done
        return true;
    }

    public void setParent(BaseEvent parent) { parentEvent = parent; }
}
