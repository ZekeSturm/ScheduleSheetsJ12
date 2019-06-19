package org.CyfrSheets.ScheduleSheets.models.exceptions;

/** Handle strings that do not produce valid data for insertion into
 *  Calendar objects. Pulled from NewEventForm in Java 8 project */
public class InvalidDateTimeArrayException extends Exception {
    public InvalidDateTimeArrayException(String s) { super(s); }
}