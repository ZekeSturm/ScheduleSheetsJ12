package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;

import java.util.ArrayList;
import java.util.Calendar;

public class ParserUtil {

    // Parse string directly to Calendar
    public static Calendar parseCalendarDateTime (String dateTimeStr) throws InvalidDateTimeArrayException {
        int[] dtA = parseDateAndTime(dateTimeStr);

        Calendar.Builder cB = new Calendar.Builder();

        cB.setDate(dtA[0], dtA[1], dtA[2]);
        cB.setTimeOfDay(dtA[3], dtA[4], dtA[5]);

        return cB.build();
    }

    // Parse string to int array ready to feed into a Calendar.Builder (Date & Time)
    public static int[] parseDateAndTime (String dateTimeStr) throws InvalidDateTimeArrayException {
        ArrayList<Integer> parsed = parseInts(dateTimeStr);
        int[] dtA = new int[6];

        if (parsed.size() == 5 || parsed.size() == 6) {
            for (int i : parsed) dtA[parsed.indexOf(i)] = i;
            if (parsed.size() == 5) dtA[5] = 0; }
        else if (parsed.size() > 6) {
            String eStr = "DateTime String/Array has too many integers:";
            for (int i : parsed) eStr += " " + i;
            throw new InvalidDateTimeArrayException(eStr); }
        else {
            String eStr = "DateTime String/Array does not have enough integers:";
            if (parsed.size() <= 0) eStr += " NO INTEGERS FOUND" + dateTimeStr;
            else for (int i : parsed) eStr += " " + i;
            throw new InvalidDateTimeArrayException(eStr); }

        // Month numbering on Calendar.Builder is 0-based
        dtA[1] -= 1;

        return dtA;
    }

    // Parse string to int array ready to feed into a Calendar.Builder (Date Only)
    public static int[] parseDate(String dateStr) throws InvalidDateTimeArrayException {
        ArrayList<Integer> parsed = parseInts(dateStr);

        int[] dA = new int[3];

        if (parsed.size() == 3) for (int i : parsed) dA[parsed.indexOf(i)] = i;
        else if (parsed.size() > 3) {
            String eStr = "Date String/Array has too many integers:";
            for (int i : parsed) eStr += " " + i;
            throw new InvalidDateTimeArrayException(eStr); }
        else {
            String eStr = "Date String/Array does not have enough integers:";
            if (parsed.size() <= 0) eStr += "NO INTEGERS FOUND - \"" + dateStr + "\"";
            else for (int i : parsed) eStr += " " + i;
            throw new InvalidDateTimeArrayException(eStr); }

        // Month numbering on Calendar.Builder is 0-based
        dA[1] -= 1;

        return dA;
    }

    // Parse string to int array ready to feed into a Calendar.Builder (Time Only)
    public static int[] parseTime(String timeStr) throws InvalidDateTimeArrayException {
        ArrayList<Integer> parsed = parseInts(timeStr);

        int[] tA = new int[3];

        if (parsed.size() == 2 || parsed.size() == 3) {
            for (int i : parsed) tA[parsed.indexOf(i)] = i;
            if (parsed.size() == 2) tA[2] = 0; }
        else if (parsed.size() > 3) {
            String eStr = "Time String/Array has too many integers:";
            for (int i : parsed) eStr += " " + i;
            throw new InvalidDateTimeArrayException(eStr); }
        else {
            String eStr = "Time String/Array does not have enough integers:";
            if (parsed.size() <= 0) eStr += " NO INTEGERS FOUND - " + timeStr;
            else for (int i : parsed) eStr = " " + i;
            throw new InvalidDateTimeArrayException(eStr); }

        return tA;
    }

    // Parse ints out of string
    public static ArrayList<Integer> parseInts(String parseThis) {
        char[] cArray = parseThis.toCharArray();
        ArrayList<Integer> output = new ArrayList<>();
        boolean lastInt = false;
        int buffer = 0;

        for (Character c : cArray) {
            if (Character.isDigit(c)) {
                if (lastInt) buffer *= 10;
                buffer += Character.getNumericValue(c);
                lastInt = true;
                continue;
            }
            output.add(buffer);
            lastInt = false;
            buffer = 0;
        }
        output.add(buffer);

        return output;
    }

    public static boolean parseBool(String parseThis) {
        if (parseThis.toLowerCase().equals("true")) return true;
        return false;
    }
}
