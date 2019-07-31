package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.util.Calendar.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

// Collection of user-created parsing methods
public class ParserUtil {

    // Find first instance of a substring within a larger string and replace it with a different substring.
    // Return input if substring is not found.
    public static String findAndReplace (String input, String find, String replace) {
        if (!input.contains(find)) return input;

        int finalIndex = input.indexOf(find) + find.length();
        if (finalIndex == input.length() - 1) {
            return input.replace(find, replace);
        } else {
            finalIndex += 1;
            String firstHalf = input.substring(0, finalIndex);
            String secondHalf = input.substring(finalIndex);
            return firstHalf.replace(find, replace) + secondHalf;
        }
    }

    // Take date/time values and offsets for each - calculate overflow.
    // Values should conform to Calendar (e.g. MONTH, HOUR one-is-zero-start, DATE_OF_MONTH correspond directly, etc)
    // Both arrays as follows (using int constant names):
    // [YEAR, MONTH, DAY_OF, HOUR, MIN, AM_PM]
    // AM_PM utilizes the AM and PM values from Calendar, or -1 if the HOUR field is in 24HR format.
    // Offset should not include/does not utilize AM_PM.
    // NOTE - Currently this method is designed purely for use with time zone offsets and overflows. If the scope of its
    // use expands beyond that, this method will become insufficient and further modifications will have to be made to
    // account for greater shifts in values (multiple year/month/day offsets, for instance).
    public static int[] dateTimeOffsetOverflow(int[] initial, int[] offsets) {
        int YEAR = 0;
        int MONTH = 1;
        int DAY_OF = 2;
        int HOUR = 3;
        int MIN = 4;
        int AM_PM = 5;

        // Handle minute offsets and any subsequent hour over/underflow
        if (offsets[MIN] != 0) {
            initial[MIN] += offsets[MIN];
            if (initial[MIN] > 59) {
                initial[MIN] -= 60;
                initial[HOUR]++;
            }
            if (initial[MIN] < 0) {
                initial[MIN] += 60;
                initial[HOUR]--;
            }
        }

        // Handle hour offsets and any subsequent date over/underflow
        if (offsets[HOUR] != 0) {
            initial[HOUR] += offsets[HOUR];
            if (initial[AM_PM] == -1) {
                if (initial[HOUR] < 0) {
                    initial[HOUR] += 24;
                    initial[DAY_OF]--;
                }
                if (initial[HOUR] > 23) {
                    initial[HOUR] -= 24;
                    initial[DAY_OF]++;
                }
            } else {
                boolean am; // Is it AM or PM? (true/false respectively - scope limited, must update overall later)
                if (initial[AM_PM] == AM) am = true;
                else am = false;
                if (initial[HOUR] < 1) {
                    initial[HOUR] += 12;
                    am = !am;
                    if (!am) initial[DAY_OF]--;
                }
                if (initial[HOUR] > 12) {
                    initial[HOUR] -= 12;
                    am = !am;
                    if (am) initial[DAY_OF]++;
                }
                // update stored AM/PM check (it is now later)
                if (am) initial[AM_PM] = AM;
                else initial[AM_PM] = PM;
            }
        }

        int[] thirties = {1, 3, 5, 8, 10}; // Month ints for months w/ 30 days (or less)

        int iMLambda = initial[MONTH]; // "effectively final" copy for lambda expression

        // Account for leap years and added days in over/underflow
        int febVal = 28;
        if (initial[YEAR] % 4 == 0 && (initial[YEAR] % 100 != 0 || initial[YEAR] % 400 == 0)) febVal = 29;

        // Handle month over/underflow
        if (initial[DAY_OF] > febVal) {
            if (initial[MONTH] == 1 || initial[DAY_OF] > 30) {
                if (IntStream.of(thirties).anyMatch(x -> x == iMLambda) || initial[DAY_OF] > 31) {
                    initial[MONTH]++;
                    if (initial[DAY_OF] > 31) initial[DAY_OF] -= 31;
                }
                if (initial[DAY_OF] > 30) initial[DAY_OF] -= 30;
            }
            if (initial[DAY_OF] > febVal) initial[DAY_OF] -= febVal;
            // May need to add "already adjusted" checks above at some point, but for current use this should work
        } else if (initial[DAY_OF] < 1) {
            initial[MONTH]--;
            switch (initial[MONTH]) {
                case 1: // February
                    initial[DAY_OF] += febVal;
                    break;
                case 3:
                case 5:
                case 8:
                case 10: // months with 30 days
                    initial[DAY_OF] += 30;
                    break;
                default: // The rest (months with 31 days)
                    initial[DAY_OF] += 31;
            }
        }

        // Handle year over/underflow
        if (initial[MONTH] > 11) {
            initial[YEAR]++;
            initial[MONTH] -= 12;
        }
        if (initial[MONTH] < 0) {
            initial[YEAR]--;
            initial[MONTH] += 12;
        }

        return initial;
    }

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
        ErrorPackage parsedEP = parseInts(dateTimeStr);

        if (parsedEP.hasError()) {
            String eStr = parsedEP.getMessage();
            throw new InvalidDateTimeArrayException(eStr);
        }

        ArrayList<Integer> parsed = (ArrayList<Integer>)parsedEP.getAux("arrayOut");
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
            if (parsed.size() <= 0) eStr += " NO INTEGERS FOUND \"" + dateTimeStr + "\"";
            else for (int i : parsed) eStr += " " + i;
            throw new InvalidDateTimeArrayException(eStr); }

        // Month numbering on Calendar.Builder is 0-based
        dtA[1] -= 1;

        return dtA;
    }

    // Parse string to int array ready to feed into a Calendar.Builder (Date Only)
    public static int[] parseDate (String dateStr) throws InvalidDateTimeArrayException {

        ErrorPackage parsedEP = parseInts(dateStr);

        if (parsedEP.hasError()) {
            String eStr = parsedEP.getMessage();
            throw new InvalidDateTimeArrayException(eStr);
        }

        ArrayList<Integer> parsed = (ArrayList<Integer>)parsedEP.getAux("arrayOut");

        int[] dA = new int[3];

        if (parsed.size() == 3) for (int i : parsed) dA[parsed.indexOf(i)] = abs(i);
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

    // Parse string to int array ready to feed into a Calendar.Builder (Time Only - 24HR format)
    public static int[] parseTime(String timeStr) throws InvalidDateTimeArrayException {

        ErrorPackage parsedEP = parseInts(timeStr);

        if (parsedEP.hasError()) {
            String eStr = parsedEP.getMessage();
            throw new InvalidDateTimeArrayException(eStr);
        }

        ArrayList<Integer> parsed = (ArrayList<Integer>)parsedEP.getAux("arrayOut");

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

    // Return a neat date/time string from a calendar. Specify which segments you want/need & time format
    public static String neatCalendarString (Calendar dateTime, boolean date, boolean time, boolean twentyFour) {
        if (!(date || time)) return "";

        String out = "";

        if (date) { // Parse out date info
            String tzId = dateTime.getTimeZone().getID().toLowerCase();
            boolean amTZ = (tzId.contains("america/") || tzId.contains("us/")); // American time zone? - for formatting
            int dateOf = dateTime.get(DAY_OF_MONTH);
            String month = null;
            int year = dateTime.get(YEAR);
            switch (dateTime.get(MONTH)) {
                case 0:
                    month = "January";
                case 1:
                    month = "February";
                case 2:
                    month = "March";
                case 3:
                    month = "April";
                case 4:
                    month = "May";
                case 5:
                    month = "June";
                case 6:
                    month = "July";
                case 7:
                    month = "August";
                case 8:
                    month = "September";
                case 9:
                    month = "October";
                case 10:
                    month = "November";
                case 11:
                    month = "December";
            }
            if (month == null) month = "MONTH_FIELD_ERROR";

            if (amTZ) {
                out += month + " " + dateOf + ", " + year;
            } else {
                out += dateOf + " " + month + ", " + year;
            }
        }

        if (time) { // Parse out time info
            if (date) out += " -- ";

            String minute = dateTime.get(MINUTE) + "";
            if (minute.length() == 1) minute = "0" + minute;

            int hourType;
            int hour;
            if (twentyFour) hourType = HOUR_OF_DAY;
            else hourType = HOUR;
            hour = dateTime.get(hourType);
            if (!twentyFour) hour++;

            out += hour + ":" + minute;

            if (!twentyFour) {
                switch (dateTime.get(AM_PM)) {
                    case AM:
                        out += " AM";
                        break;
                    case PM:
                        out += " PM";
                }
            }
        }
        return out;
    }

    // Shorthand of parent - choose either date or time, defaults to 12 hour if latter
    public static String neatCalendarString (Calendar dateTime, boolean date, boolean time) {
        return neatCalendarString(dateTime, date, time, false);
    }

    // Shorthand of parent - defaults to whole string, allows hour formatting
    public static String neatCalendarString (Calendar dateTime, boolean twentyFour) {
        return neatCalendarString(dateTime, true, true, twentyFour);
    }

    // Shorthand of parent - defaults to whole string & 12 hour
    public static String neatCalendarString (Calendar dateTime) {
        return neatCalendarString(dateTime, false);
    }

    // Parse all ints out of string - Return w/ leftover non-int string fragments if stringReturn is true
    public static ErrorPackage parseInts (String parseThis, boolean stringReturn) {

        // Check for empty input
        if (parseThis.isEmpty()) return yesError("Input string is empty!");


        String subParse = parseThis;                        // Introduce substring to be parsed out so as not to
                                                            // destroy input string
        ErrorPackage out = yesError("placeholder");         // Initialize as if an error has occurred - use to check
                                                            // for inputs
        int lastIndex = parseThis.length() - 1;             // Last available index for input string
        ArrayList<Integer> intArray = new ArrayList<>();    // Output int array
        ArrayList<String> stringFrags = new ArrayList<>();  // Output string array. Only needed for stringReturn

        // Iterate through parseThis w/ parseSingleInt - w/ retPos, reassign subParse to be the un-parsed substring.
        // Will break w/ return handler when end is reached w/o int, or if last index is reached
        while (true) {
            ErrorPackage handler = parseSingleInt(subParse, true);

            // No more ints in string
            if (handler.hasError()) {
                if (out.hasError()) { // If nothing was placed in out
                    return handler;
                } else if (stringReturn) stringFrags.add(subParse);
                return out;
            }

            // Properly initialize out if it hasn't been and add int to array
            if (out.hasError()) out = noError();

            intArray.add((int)handler.getAux("intOut"));
            int i = (int)handler.getAux("lastIndex");

            if (stringReturn) stringFrags.add((String)handler.getAux("priorString"));

            // Check if there will be further characters to parse - if so, continue
            if (i < lastIndex) {
                subParse = subParse.substring(i + 1);
                lastIndex = subParse.length() - 1;
                continue;
            }

            // Finalize output
            out.addAux("arrayOut", intArray);
            out.addAux("stringFrags", stringFrags);

            return out;
        }
    }

    // Parse all integers out of string - does not return string fragments
    public static ErrorPackage parseInts(String parseThis) { return parseInts(parseThis, false); }

    // Parse single integer out of string, return the rest of the string as fragments.
    // Returns only string before int when retPos is true.
    // The purpose of retPos is to shorten execution length over multiple instances - namely w/in parseInts
    public static ErrorPackage parseSingleInt (String parseThis, boolean retPos) {
        // Check for empty input
        if (parseThis.isEmpty()) return yesError("Input string is empty!");

        char[] cArray = parseThis.toCharArray();
        ArrayList<String> sOutput = new ArrayList<>();
        boolean lastInt = false;
        boolean intFound = false;
        boolean lastChar = false;
        int buffer = 0;
        String sBuffer = "";

        ErrorPackage out = yesError("PLACEHOLDER"); // Stifle "may not have been initialized" exc. w/ placeholder init

        int negative = 1;

        for (int i = 0; i < cArray.length; i++) {
            char c = cArray[i];
            if (Character.isDigit(c) && !intFound) {
                if (lastInt) buffer *= 10;
                else if (lastChar) {
                    lastChar = false;
                    if (!retPos) {
                        sOutput.add(sBuffer);
                        sBuffer = "";
                    }
                }
                buffer += Character.getNumericValue(c);
                lastInt = true;
                continue;
            }
            if (lastInt) {
                // return index if retPos w/ initial substring
                out = noError();
                if (retPos) {
                    out.addAux("intOut", buffer * negative);
                    out.addAux("lastIndex", i);
                    out.addAux("priorString", sBuffer);
                }
                lastInt = false;
                intFound = true;
            } else if (c == '-' && !intFound) {
                negative = -1;
            } else {
                sBuffer += c;
                lastChar = true;
            }
        }

        if (!intFound && !lastInt) return yesError("No integers in string - " + parseThis);
        else {
            if (!intFound) {
                out = noError();
                out.addAux("lastIndex", cArray.length);
            }
            out.addAux("intOut", buffer * negative);
            out.addAux("stringFrags", sOutput);
            return out;
        }
    }

    // Quick Implementation - assumes retPos
    public static ErrorPackage parseSingleInt(String parseThis) { return parseSingleInt(parseThis, true); }

    // Output a string from a byte in a specifically formatted way
    // TODO - May be able to make this private. Check when backend work done
    public static String parseByteToString (byte[] parseThis) {
        if (parseThis == null) return "NULL POINTER";
        // Output string
        String out = "";
        for (int i = 0; i < parseThis.length; i++) {
            out += Byte.toString(parseThis[i]);
            if (i < parseThis.length - 1) out += "]|[";
        }
        return out;
    }

    // Reverse the above
    // TODO - Method may be useless. Consider trimming when backend work is done
    public static byte[] parseByteFromString (String parseThis) {
        // Split string into array to parse
        char[] chArray = parseThis.toCharArray();

        // Get # of bytes in string by "border proxy" - count ], |, and [
        // If numbers are not equal this is not a valid string
        int endCount = 0; // ] count
        int midCount = 0; // | count
        int startCount = 0; // [ count
        for (char c : chArray) {
            if (c == ']') endCount++;
            if (c == '|') midCount++;
            if (c == '[') startCount++;
        }
        if (endCount != midCount || midCount != startCount || startCount != endCount) return new byte[] {-128, 127, -127, -128, 127, 0};

        byte[] out = new byte[endCount + 1]; // initialize output array w/ one more spot than there are dividers

        ArrayList<Byte> outAL = new ArrayList<>();
        boolean primed = true;      // Boolean to check for ] (unprime, false) and [ (prime, true) to separate out bytes
        String buffer = "";

        int i = 0; // Byte index counter

        for (char c: chArray) {

            switch (c) {
                case ']':
                    primed = false;
                    try {
                        out[i] = Byte.valueOf(buffer);
                        i++;
                        buffer = "";
                        continue;
                    } catch (NumberFormatException e) { return new byte[] {-128, 127, -127, -128, 127, 0}; }
                case '[':
                    primed = true;
                    continue;
                default:
                    if (primed) buffer += c;
            }
        }
        // add last byte
        out[i] = Byte.valueOf(buffer);

        return out;
    }

    // Checks bytes against specifically formatted strings from above method
    public static boolean checkByteAgainstString (byte[] checkThis, String parsedThis) {
        if (checkThis != null && !parsedThis.isEmpty())
            return parsedThis.equals(parseByteToString(checkThis));
        return false;
    }

    public static boolean equalsAny(String in, String[] args) {
        for (String s : args) {
            if (in.equals(s)) return true;
        }
        return false;
    }

    public static boolean equalsAll(String in, String[] args) {
        for (String s : args) {
            if (!in.equals(s)) return false;
        }
        return true;
    }
}
