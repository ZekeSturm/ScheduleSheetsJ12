package org.CyfrSheets.ScheduleSheets.models.utilities;

public class ExtraUtil {

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
