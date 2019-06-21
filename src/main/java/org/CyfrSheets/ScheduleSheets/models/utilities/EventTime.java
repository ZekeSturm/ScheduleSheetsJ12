package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.util.Calendar;

public class EventTime {

    Calendar startTime;
    Calendar endTime;

    // Store user times here for planning events

    boolean hasEndTime;

    public boolean hasEnd() { return hasEndTime; }

    public boolean multiDay() {
        // TODO - return if startTime day and endTime day are not equal

        // Error stifling - remove when done
        return true;
    }
}
