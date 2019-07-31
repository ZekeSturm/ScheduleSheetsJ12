package org.CyfrSheets.ScheduleSheets.models.forms;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.checkClass;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

public class StaticEventForm {

    private String eventName = "";

    private String eventDesc = "";

    private String startDate = "";

    private String startTime = "";

    private Calendar startCal = null;

    private boolean startTimeInitialized = false; // Start Time Calendar initialized and populated

    private String hasEnd = "false";

    private String endDate = "";

    private String endTime = "";

    private Calendar endCal = null;

    private boolean endTimeInitialized = false; // End Time Calendar initialized and populated

    // Temporary User creator name/password
    private String cName = "";

    private String cPass = "";

    private String pConfirm = "";

    public String getEventName() { return eventName; }
    public String getEventDesc() { return eventDesc; }
    public String getCName()     { return cName; }
    public String getCPass()     { return cPass; }
    public String getPConfirm()  { return pConfirm; }

    public String getStartDate() { return startDate; }
    public String getStartTime() { return startTime; }

    public String getHasEnd()      { return hasEnd; }
    public boolean hasEndBool()    { return hasEnd.toLowerCase().equals("true"); }

    public String getEndDate()   { return endDate; }
    public String getEndTime()   { return endTime; }

    public Calendar getStart()   { return startCal; }
    public Calendar getEnd()     { return endCal;}

    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setEventDesc(String eventDesc) { this.eventDesc = eventDesc; }
    public void setCName(String cName)         { this.cName = cName; }
    public void setCPass(String cPass)         { this.cPass = cPass; }
    public void setPConfirm(String pConfirm)   { this.pConfirm = pConfirm; }

    public void setHasEnd(String hasEnd)   { this.hasEnd = hasEnd; }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        startConvert();
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
        startConvert();
    }
    public void setEndDate(String endDate)     { this.endDate = endDate; }
    public void setEndTime(String endTime)     { this.endTime = endTime; }


    // Returns true if calendar object exists - if not, error has occurred
    public boolean startData() {
        startConvert();
        return startTimeInitialized;
    }
    public boolean endData() {
        endConvert();
        return endTimeInitialized;
    }

    // Will return a map of fields indicating which ones are and are not missing
    public ArrayList<String> whatsMissing() {
        startConvert();
        endConvert();
        ArrayList<String> out = new ArrayList<>();
        for (Field f : this.getClass().getDeclaredFields()) {
            boolean present;
            String fN = f.getName();
            try {
                Object o = f.get(this);
                switch (checkClass(o)) {
                    case STRING:
                        present = !(f.get(this).equals(""));
                        break;
                    case CALENDAR:
                        present = f.get(this) != null;
                        break;
                    case BOOLEAN:
                        present = f.getBoolean(this);
                        break;
                    default:
                        present = false;
                }
            } catch (IllegalAccessException e) {
                present = false;
                e.printStackTrace();
            }
            if (!present) out.add(fN);
        }
        return out;
    }

    public boolean passMatch() { return cPass.equals(pConfirm); }

    public StaticEventForm(boolean logged) {
        if (logged) {
            cName = "";
            cPass = "";
            pConfirm = "";
        }
    }

    public StaticEventForm() { }

    // Below methods convert string inputs to calendar outputs automatically once data is collected
    private void startConvert() {
        if (startInc()) return; // Terminate method if data is missing
        if (startTimeInitialized) return; // Terminate method if calendar object is initialized

        try {
            int[] sDA = parseDate(startDate);
            int[] sTA = parseTime(startTime);

            Calendar.Builder cb = new Calendar.Builder();
            cb.setCalendarType("gregorian");

            cb.setDate(sDA[0], sDA[1], sDA[2]);
            cb.setTimeOfDay(sTA[0], sTA[1], sTA[2]);

            startCal = cb.build();

            startTimeInitialized = true;
        } catch (InvalidDateTimeArrayException e) { return; }
    }

    private void endConvert() {
        if (endInc()) return; // Terminate method if data is missing
        if (endTimeInitialized) return; // Terminate method if calendar object is initialized

        try {
            int[] eDA = parseDate(endDate);
            int[] eTA = parseTime(endTime);

            Calendar.Builder cb = new Calendar.Builder();
            cb.setCalendarType("gregorian");

            cb.setDate(eDA[0], eDA[1], eDA[2]);
            cb.setTimeOfDay(eTA[0], eTA[1], eTA[2]);

            endCal = cb.build();

            endTimeInitialized = true;
        } catch (InvalidDateTimeArrayException e) { return; }
    }

    // Returns false if all necessary data is present
    private boolean startInc() { return (startDate.equals("") || startTime.equals("")); }

    private boolean endInc()   { return (endDate.equals("") || endTime.equals("")); }

}
