package org.CyfrSheets.ScheduleSheets.models.utilities;

import javax.persistence.*;
import java.util.Calendar;

import static java.util.Calendar.*;

@Embeddable
public class EventTime {

    Calendar startTime;
    Calendar endTime;

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

    public EventTime() { }

    public boolean hasEnd() { return hasEndTime; }

    public boolean multiDay() {
        if (hasEnd()) {
            if (startTime.get(YEAR) == endTime.get(YEAR))
                if (startTime.get(MONTH) == endTime.get(MONTH))
                    if (startTime.get(DATE) == endTime.get(DATE))
                        return false;

            return true;
        } else return false;
    }
}
