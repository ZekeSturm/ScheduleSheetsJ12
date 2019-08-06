package org.CyfrSheets.ScheduleSheets.models.utilities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Calendar.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.EventType.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

@Embeddable
public class EventTime {

    Calendar startTime;
    Calendar endTime;

    EventType parentType;

    // Store user times here for planning events
    // TODO - Access and utilization methods
    ArrayList<TimeSlot> userBlocks;

    boolean hasEndTime;

    public EventTime(Calendar startTime, Calendar endTime, boolean statEv) {
        this.startTime = startTime;
        this.endTime = endTime;
        hasEndTime = true;
        if (startTime.get(DAY_OF_MONTH) != endTime.get(DAY_OF_MONTH)) {
            if (statEv) parentType = MDS;
            else parentType = MDP;
        } else {
            if (statEv) parentType = SDS;
            else parentType = SDP;
        }
    }

    public EventTime(Calendar startTime, Calendar endTime) {
        this(startTime, endTime, false);
    }

    public EventTime(Calendar startTime) {
        this.startTime = startTime;
        hasEndTime = false;
        parentType = SOS;
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

    // Fetch a clean string of the starting time for the event as a whole
    public String printStartTime(TimeZone clientTZ, boolean twentyFour) {
        return printTime(startTime, clientTZ, twentyFour);
    }

    // As above, but assuming server time/no offset & 12hr
    public String printStartTime() {
        return neatCalendarString(startTime, true, false) + " at " + neatCalendarString(startTime, false, true);
    }

    // Fetch a clean string of the end time for the event as a whole. Returns "NO_END_SET" if... well.
    public String printEndTime(TimeZone clientTZ, boolean twentyFour) {
        if (!hasEndTime) return "NO_END_SET";
        return printTime(endTime, clientTZ, twentyFour);
    }

    // As above, but assuming server time/no offset & 12hr
    public String printEndTime() {
        if (!hasEndTime) return "NO_END_SET";
        return neatCalendarString(endTime, true, false) + " at " + neatCalendarString(endTime, false, true);
    }

    // Fetch the end time of the event ONLY - for single day events
    public String printEndTimeNoDate(TimeZone clientTZ, boolean twentyFour) {
        if (!hasEndTime) return "NO_END_SET";
        return printTimeOnly(clientTZ, twentyFour);
    }

    // As above, but assuming server time/no offset & 12hr
    public String printEndTimeNoDate() {
        if (!hasEndTime) return "NO_END_SET";
        return neatCalendarString(endTime, false, true);
    }

    // Mechanics of the above
    private String printTime(Calendar time, TimeZone clientTZ, boolean twentyFour, boolean endSD) {
        double clientUTCO = clientTZ.getRawOffset() *  3.6e6; // Get client UTC/GMT offset in hours
        double serverUTCO = time.getTimeZone().getRawOffset() * 3.6e6; // get server UTC/GMT offset in hours

        double scOffset = clientUTCO - serverUTCO; // Time offset between server and client
        int hourOffset = (int)scOffset; // Hour of scOffset
        int minOffset = (int)((scOffset % hourOffset) * 60); // Offset by minutes in the rare case of hour-fractional time zone changes

        // fetch base server time fields
        int serverDayOf = time.get(DAY_OF_MONTH);
        int serverMonth = time.get(MONTH);
        int serverYear = time.get(YEAR);
        int hourType;
        if (twentyFour) hourType = HOUR_OF_DAY;
        else hourType = HOUR;
        int serverHour = time.get(hourType);
        int serverMin = time.get(MINUTE);

        int amPM;
        if (twentyFour) amPM = -1;
        else amPM = time.get(AM_PM);

        int[] serverInitial = {serverYear, serverMonth, serverDayOf, serverHour, serverMin, amPM};
        int[] offset = {0, 0, 0, hourOffset, minOffset};

        int[] postOffset = dateTimeOffsetOverflow(serverInitial, offset);

        Calendar timeOut = new GregorianCalendar();

        timeOut.setTimeZone(clientTZ);

        timeOut.set(YEAR, postOffset[0]);
        timeOut.set(MONTH, postOffset[1]);
        timeOut.set(DAY_OF_MONTH, postOffset[2]);
        timeOut.set(hourType, postOffset[3]);
        timeOut.set(MINUTE, postOffset[4]);
        if (!twentyFour) timeOut.set(AM_PM, postOffset[5]);

        // Confirm offset did not make event "multi-day" if "printTimeOnly" in use - accommodate if it did
        if (endSD) {
            if (timeOut.get(DAY_OF_MONTH) == time.get(DAY_OF_MONTH)) {
                return neatCalendarString(timeOut, false, true);
            } else {
                return neatCalendarString(timeOut, false, true) + " the following day.";
            }
        }

        // Fetch string output
        String output = neatCalendarString(timeOut, true, true, twentyFour);

        // Setup Regex
        Pattern dtParti = Pattern.compile("--");
        Matcher outMatch = dtParti.matcher(output);

        // Format for cleanliness
        if (parentType.isOneDay() && !parentType.startOnly()) {
            output = outMatch.replaceAll("from");
        } else {
            output = outMatch.replaceAll("at");
        }

        return output;
    }

    // Same as above, used by everything EXCEPT printTimeOnly's implementation
    private String printTime(Calendar time, TimeZone clientTZ, boolean twentyFour) {
        return printTime(time, clientTZ, twentyFour, false); }

    // Same mechanics returning only the time portion of the string - for single day event end times
    private String printTimeOnly(TimeZone clientTZ, boolean twentyFour) {
        return printTime(endTime, clientTZ, twentyFour, true);
    }
}
