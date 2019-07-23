package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;

import java.util.ArrayList;
import java.util.Calendar;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

// Collection of user-created parsing methods
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
            if (parsed.size() <= 0) eStr += " NO INTEGERS FOUND" + dateTimeStr;
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

        for (int i = 0; i < cArray.length; i++) {
            char c = cArray[i];
            if ((Character.isDigit(c) || c == '-') && !intFound) {
                if (lastInt) buffer *= 10;
                else if (lastChar) {
                    lastChar = false;
                    if (!retPos) {
                    sOutput.add(sBuffer);
                    sBuffer = ""; }}
                buffer += Character.getNumericValue(c);
                lastInt = true;
                continue;
            }
            if (lastInt) {
                // return index if retPos w/ initial substring
                out = noError();
                if (retPos) {
                    out.addAux("intOut", buffer);
                    out.addAux("lastIndex", i);
                    out.addAux("priorString", sBuffer);
                }
                lastInt = false;
                intFound = true;
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
            out.addAux("intOut", buffer);
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
