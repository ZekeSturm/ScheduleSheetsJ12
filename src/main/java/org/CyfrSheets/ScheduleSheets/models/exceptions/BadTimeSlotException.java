package org.CyfrSheets.ScheduleSheets.models.exceptions;

/** Indicates Time Slot creation failure */
public class BadTimeSlotException extends Exception {
    public BadTimeSlotException(String s) { super(s); }
}
