package org.CyfrSheets.ScheduleSheets.models.forms;

public class StaticEventForm {

    private String eventName;

    private String eventDesc;

    private String startDate;

    private String startTime;

    private String endDate;

    private String endTime;

    // Temporary User creator name/password
    private String cName;

    private String cPass;

    private String pConfirm;

    public String getEventName() { return eventName; }
    public String getEventDesc() { return eventDesc; }
    public String getStartDate() { return startDate; }
    public String getStartTime() { return startTime; }
    public String getEndDate()   { return endDate; }
    public String getEndTime()   { return endTime; }
    public String getCName()     { return cName; }
    public String getCPass()     { return cPass; }

    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setEventDesc(String eventDesc) { this.eventDesc = eventDesc; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndDate(String endDate)     { this.endDate = endDate; }
    public void setEndTime(String endTime)     { this.endTime = endTime; }
    public void setCName(String cName)         { this.cName = cName; }
    public void setCPass(String cPass)         { this.cPass = cPass; }
    public void setPConfirm(String pConfirm)   { this.pConfirm = pConfirm; }

    public boolean passMatch() { return cPass.equals(pConfirm); }

    public StaticEventForm(boolean logged) {
        if (logged) {
            cName = "registeredUser";
            cPass = "12345";
        }
    }

    public StaticEventForm() { }
}
