package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.exceptions.BadTimeSlotException;
import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;

import java.util.Calendar;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

/** Time Slot for passing along individual user time segments. [Future Me - Consider implementing Attendance Status field] */
public class TimeSlot {

    private Calendar startTime;
    private Calendar endTime;

    private Participant parent;

    public TimeSlot(String startTime, String endTime, Participant parent) throws BadTimeSlotException {
        Calendar.Builder cB = new Calendar.Builder();
        cB.setCalendarType("gregorian");
        // Error checking boolean
        boolean startSuccess = false;
        try {
            // Convert strings to Calendars w/ ParserUtil
            this.startTime = parseCalendarDateTime(startTime);
            startSuccess = true;
            this.endTime = parseCalendarDateTime(endTime);

            this.parent = parent;

        } catch (InvalidDateTimeArrayException e) {
            String eMsg = "TimeSlot creation failed - InvalidDateTimeArrayException in ";
            if (startSuccess) eMsg += "endTime input - ";
            else eMsg += "startTime input - ";
            eMsg += e.getMessage();
            throw new BadTimeSlotException(eMsg);
        }
    }
}
