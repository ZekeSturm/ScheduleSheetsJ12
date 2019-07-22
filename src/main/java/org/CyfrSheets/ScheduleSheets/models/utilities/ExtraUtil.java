package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidPasswordException;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.springframework.stereotype.Component;

@Component
public class ExtraUtil {

    private static int nextUID = 1;

    public static RegUser getUserWithID(String name, String pass, String emailAddr) throws InvalidPasswordException {
        RegUser out = new RegUser(name, pass, emailAddr, nextUID);
        nextUID++;
        return out;
    }

    public static boolean checkByteEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    public static byte[] byteConcat(byte[] a, byte[] b) {
        int sub = 0;
        int len = a.length + b.length;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            if (sub == a.length) sub = 0;
            if (i < a.length) out[i] = a[sub];
            else out[i] = b[sub];
            sub++;
        }
        return out;
    }
}
