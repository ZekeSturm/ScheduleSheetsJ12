package org.CyfrSheets.ScheduleSheets.models;

public enum EventType {
    SOS ("Start Time Only, Static", false, true, true),
    SDS ("Single Day, Static", false, false, true),
    SDP ("Single Day, Planning", true, false, true),
    MDS ("Multi Day, Static", false, false, false),
    MDP ("Multi Day, Planning", true, false, false);

    private final String typeString;
    private final boolean oneTime; // StartOnly essentially
    private final boolean oneDay;
    private final boolean planning;

    EventType (String typeString, boolean planning, boolean oneTime, boolean oneDay) {
        this.typeString = typeString;
        this.planning = planning;
        this.oneTime = oneTime;
        this.oneDay = oneDay;
    }

    public String getType() { return typeString; }

    public boolean isOneDay() { return oneDay; }

    public boolean isPlanning() { return planning; }

    public boolean startOnly() { return oneTime; }
}