package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.springframework.stereotype.Component;

// TODO - File useless after method migration. Delete after next commit

@Component
public class ExtraUtil {

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
