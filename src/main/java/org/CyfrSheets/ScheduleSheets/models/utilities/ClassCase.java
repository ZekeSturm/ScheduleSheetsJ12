package org.CyfrSheets.ScheduleSheets.models.utilities;

public enum ClassCase {

    // Add more cases to the ClassChecker switch if adding enums
    BOOLEAN ("bool"),
    STRING ("str"),
    INTEGER ("int"),
    REGUSER ("regu"),
    TEMPUSER ("temu"),
    UNKNOWN ("uknw");

    private String indicator;

    ClassCase(String s) { indicator = s; }

    public String getIndicator() { return indicator; }
}
