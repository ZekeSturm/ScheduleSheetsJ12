package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.util.HashMap;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassCase.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.checkClass;

public class ErrorPackage {

    // Data stored w/ name string keys
    private HashMap<String, Object> dataMap;

    // Consider expanding ClassCase/Checker and implementing a hashmap using the same keys as the dataMap to store the data types

    // Auxiliary/Overflow message counter
    int auxMsgNum = 0;

    // Use same as above but for additional error messages
    int auxErrNum = 0;

    public ErrorPackage(String message, boolean error) {
        // Initialize dataMap
        dataMap = new HashMap<>();

        // Add error message and indicator boolean
        dataMap.put("message", message);
        dataMap.put("error", error);
    }

    // Empty constructor defaults to no error
    public ErrorPackage() { this("No Error!", false); }

    public String getMessage() {
        if (dataMap.containsKey("message") && checkClass(dataMap.get("message")) == STRING) return (String)dataMap.get("message");
        else return "No message found! How'd you do that?";
    }

    public String setMessage(String msg) {
        if (dataMap.containsKey("error")) {
            if (!(Boolean)dataMap.get("error")) {
                auxMsgNum += 1;
                dataMap.put("auxMsg" + auxMsgNum, msg);
                return "Error message already present! Message saved as Aux " + auxMsgNum +
                        "and can be retrieved with getAuxMsg(" + auxMsgNum +")";
            }
        }
        dataMap.put("message", msg);
        return "true";
    }

    public boolean hasError() {
        if (dataMap.containsKey("error") && checkClass(dataMap.get("error")) == BOOLEAN)
            return (Boolean)dataMap.get("error");
        return false;
    }

    public String getAuxMsg(int auxNum) {
        if (dataMap.containsKey("auxMsg" + auxNum) && checkClass(dataMap.get("auxMsg" + auxNum)) == STRING)
            return (String)dataMap.get("auxMsg" + auxNum);
        return "Could not find this auxiliary message!";
    }

    public boolean addAux(String key, Object value) {
        // lowercase for comparison
        String chKey = key.toLowerCase();

        // Check for reserved fields
        if (chKey.substring(0, 5).equals("auxmsg") || chKey.equals("message") || chKey.equals("error")) return false;
        // Check for existing fields - make separate method for overrides
        if (dataMap.containsKey(key)) return false;

        dataMap.put(key, value);
        return true;
    }

    // Retrieval w/ type enforcement via ClassCase
    public Object getAux(String key, ClassCase cc) {
        String chKey = key.toLowerCase();
        if (chKey.substring(0, 5).equals("auxmsg") || chKey.equals("message") || chKey.equals("error")) return "These values are not retrievable via this method";
        if (dataMap.containsKey(key)) {
            Object out = dataMap.get(key);
            if (checkClass(out) == cc) return out;
            return yesError("This auxiliary is not of the given type");
        }
        return "This auxiliary key was not found";
    }

    // Retrieval
    public Object getAux(String key) {
        // see addAux for notes
        String chKey = key.toLowerCase();
        if (chKey.substring(0, 5).equals("auxmsg") || chKey.equals("message") || chKey.equals("error")) return "These values are not retrievable via this method";
        if (dataMap.containsKey(key)) return dataMap.get(key);
        return "This auxiliary key was not found";
    }

    // Simple static methods for cleaner execution/less handling via constructor
    public static ErrorPackage noError(String msg, boolean ancil) {
        ErrorPackage out = new ErrorPackage(msg, false);
        out.addAux("ancil", ancil);
        return out;
    }

    public static ErrorPackage noError(boolean ancil) {
        ErrorPackage out = noError();
        out.addAux("ancil", ancil);
        return out;
    }

    public static ErrorPackage noError() { return new ErrorPackage("No Error!", false); }

    public static ErrorPackage yesError(String msg, String ancil) {
        ErrorPackage out = yesError(msg);
        out.addAux("ancil", ancil);
        return out;
    }

    public static ErrorPackage yesError(String msg) { return new ErrorPackage(msg, true); }

}
