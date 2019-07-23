package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.lang.reflect.Array;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassCase.*;

// TODO - Implement this class en masse to check all casting operations

public class ClassChecker {

    // Returns an enum from ClassCase that matches the given object's class, if it exists
    public static ClassCase checkClass(Object o) {
        if (o == null) return UNKNOWN;
        // Fetch plaintext class name
        String s = o.getClass().getSimpleName().toLowerCase();
        return checkClass(s);
    }

    // Returns an enum from ClassCase that matches the given class, if it exists
    public static ClassCase checkClass(Class<?> c) {
        if (c == null) return UNKNOWN;
        // Fetch plaintext class name
        String s = c.getSimpleName().toLowerCase();
        return checkClass(s);
    }

    // Returns an enum from ClassCase that matches the given class string, if it exists
    private static ClassCase checkClass(String s) {

        // Add cases when adding further classes to enum
        switch (s) {
            case "string":
                return STRING;
            case "boolean":
                return BOOLEAN;
            case "integer":
                return INTEGER;
            case "participant":
                return PARTICIPANT;
            case "reguser":
                return REGUSER;
            case "tempuser":
                return TEMPUSER;
            case "errorpackage":
                return ERRORPACKAGE;
            case "byte[]":
                return HASH;
            case "gregoriancalendar":
                return CALENDAR;

            case "object":
                return OBJECT;
            default:
                return UNKNOWN;
        }
    }

    // Compare two object classes - save input to output if they are the same (excluding the UNKNOWN enum).
    // Input and Output must be passed as an array to preserve saved information - retrieve from this array after call
    public static boolean checkClassThenSet(Object[] inOut) {
        // If the input and output share the same class...
        if (sameSansUKNWN(inOut[0], inOut[1])) {
            ClassCase inCase = checkClass(inOut[0]);
            // In case someone passes an array to this one instead
            if (inCase.isArray) return cloneArray(inOut[0], inOut[1]);
            Array.set(inOut, 1, inOut[0]);
            return true;
        } else return false;
    }

    // Compare the array types then clone input to output. Should be called instead of the above if one knows an object
    // is an array
    public static boolean checkArrayClassThenSet(Object input, Object output) {
        if (!sameSansUKNWN(input, output)) return false;
        if (!input.getClass().isArray() || !output.getClass().isArray()) return false;
        return cloneArray(input, output);
    }

    // Compare two object classes
    public static boolean compareClasses(Object a, Object b) { return sameSansUKNWN(a, b); }

    // Clone one array to the other from generic object input.
    private static boolean cloneArray(Object input, Object output) {
        // Ensure input/output are actually arrays
        if (!input.getClass().isArray() || !output.getClass().isArray()) return false;

        // Get lengths of arrays
        int inLen = Array.getLength(input);
        int outLen = Array.getLength(output);

        if (inLen != outLen) return false;
        for (int i = 0; i < inLen; i++) Array.set(output, i, Array.get(input, i));
        return true;
    }

    // Pull a definite array object out of an object if it is an array - return null if it is not an array.
    // Not using this presently but it may come in handy later so I'm keeping it
    private static Object[] arrayRip(Object o) {
        // If object is array, fetch what the array is of
        Class arrayOf = o.getClass().getComponentType();

        // If object is not an array, above returns null
        if (arrayOf == null) return null;

        // Check if array is of primitive types that cannot be automatically cast to Object
        if (arrayOf.isPrimitive()) { // Manually cast via iteration if so...
            int len = Array.getLength(o);
            Object[] out = new Object[len];
            for (int i = 0; i < len; i++) out[i] = Array.get(o, i);
            return out;
        } else return (Object[])o; // And cast broadly if not
    }

    // Compare two object classes - return true if they are the same and neither are UNKNOWN
    private static boolean sameSansUKNWN(Object a, Object b) {
        if (checkClass(a) == checkClass(b) && checkClass(a) != UNKNOWN) return true;
        return false;
    }
}