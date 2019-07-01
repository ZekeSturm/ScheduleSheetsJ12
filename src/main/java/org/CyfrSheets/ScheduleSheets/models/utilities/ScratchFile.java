package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.checkClassThenSet;

public class ScratchFile {

    public static void main(String[] args) {
        byte[] testIn = new byte[32];
        byte[] testOut = new byte[32];

        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

            sr.nextBytes(testIn);

            for (int i = 0; i < 32; i++) {
                System.out.println(testIn[i] + " | " + testOut[i]);
            }
            System.out.println("");
            System.out.println(testIn.getClass().getSimpleName());
            System.out.println("");

            if (checkClassThenSet(testIn, testOut)) {
                for (int i = 0; i < 32; i++) {
                    System.out.println(testIn[i] + " | " + testOut[i]);
                }
            } else System.out.println("Nope");
        } catch (NoSuchAlgorithmException e) { }
    }
}
