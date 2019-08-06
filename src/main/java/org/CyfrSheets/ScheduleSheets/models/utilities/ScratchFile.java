package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

// TODO - Delete this file once majority testing is done
public class ScratchFile {

    /** public static void main(String[] args) {
        boolean a = true;
        Boolean b = false;

        Boolean[] inOut = {a, b};

        System.out.println(a + " | " + b);
        System.out.println("------------");

        checkClassThenSet(inOut);

        System.out.println(a + " | " + b);
        System.out.println("------------");

        a = inOut[0];
        b = inOut[1];

        System.out.println(a + " | " + b);
    } */

    /**
    public static void main(String[] args) {
        byte[] testIn = new byte[32];

        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

            Byte tb1 = Byte.valueOf("123");
            Byte tb2 = Byte.valueOf("-21");
            Byte tb3 = Byte.valueOf("54");

            System.out.println(tb1);
            System.out.println(tb2);
            System.out.println(tb3);

            sr.nextBytes(testIn);

            byte[] testOut = parseByteFromString(parseByteToString(testIn));

            for (int i = 0; i < 32; i++) {
                System.out.println(testIn[i] + " | " + testOut[i]);
            }

            System.out.println();
            System.out.println(testIn);
            System.out.println(testOut);
        } catch (NoSuchAlgorithmException e) { }
    } */

    public static void main(String[] args) {
        for (String s: args) {
            InputSanit.sanitize(s);
            System.out.println();
        }
    }
}
