package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.CyfrSheets.ScheduleSheets.models.users.TempUser;

public enum ClassCase {

    // Add more cases to the ClassChecker switch if adding enums
    BOOLEAN ("bool", Boolean.class, false),
    STRING ("str", String.class, false),
    INTEGER ("int", Integer.class, false),
    PARTICIPANT ("part", Participant.class, false),
    REGUSER ("regu", RegUser.class, false),
    TEMPUSER ("temu", TempUser.class, false),
    ERRORPACKAGE("erpk", ErrorPackage.class, false),
    HASH ("hash", byte[].class, true),

    UNKNOWN ("uknw", null, false);

    public final String indicator;

    public final Class<?> type;

    public final boolean isArray;


    ClassCase(String s, Class<?> ct, boolean array) {
        indicator = s;
        type = ct;
        isArray = array;
    }
}
