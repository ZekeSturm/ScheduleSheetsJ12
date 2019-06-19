package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.util.HashMap;

public class ErrorPackage {

    // Data stored w/ name string keys
    private HashMap<String, Object> dataMap;

    // Auxiliary/Overflow message counter
    int auxMsgNum = 0;

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
        if (dataMap.containsKey("message")) return (String)dataMap.get("message");
        else return "No message found! How'd you do that?";
    }

    public String setMessage(String msg) {
        if (dataMap.containsKey("error")) {
            if (!(Boolean)dataMap.get("error")) {
                auxMsgNum += 1;
                dataMap.put("auxMsg" + auxMsgNum, msg);
                return "Error message already present! Message saved as Aux " + auxMsgNum + "and can be retrieved with getAuxMsg(" + auxMsgNum +")";
            }
        }
        dataMap.put("message", msg);
        return "true";
    }

    public boolean hasError() {
        if (dataMap.containsKey("error")) {
            return (Boolean)dataMap.get("error");
        }
        return false;
    }

    public String getAuxMsg(int auxNum) {
        if (dataMap.containsKey("auxMsg" + auxNum)) return (String)dataMap.get("auxMsg" + auxNum);
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

    public Object getAux(String key) {
        // see addAux for notes
        String chKey = key.toLowerCase();
        if (chKey.substring(0, 5).equals("auxmsg") || chKey.equals("message") || chKey.equals("error")) return "These values are not retrievable via this method";
        if (dataMap.containsKey(key)) return dataMap.get(key);
        return "This auxiliary key was not found";
    }

    // Simple static methods for cleaner execution/less handling via constructor
    public static ErrorPackage noError(boolean ancil) {
        ErrorPackage out = new ErrorPackage("No Error!", false);
        out.addAux("ancil", ancil);
        return out;
    }

    public static ErrorPackage noError(String msg, boolean ancil) {
        ErrorPackage out = new ErrorPackage(msg, false);
        out.addAux("ancil", ancil);
        return out;
    }

    public static ErrorPackage yesError(String msg) { return new ErrorPackage(msg, true); }

}
