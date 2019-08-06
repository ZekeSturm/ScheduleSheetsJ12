package org.CyfrSheets.ScheduleSheets.models.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

public class InputSanit {

    // Stores patterns to be checked against - draws from updatable text files
    private static List<Pattern> patternListHTML;
    private static List<Pattern> patternListSQL;
    private static List<Pattern> patternListJS;
    private static boolean plInit = false;
    private static FileTime lastMod;

    private static Pattern commentMarker = Pattern.compile("//-/");

    // Base file path for text folder where sanitization pattern files are stored
    private static String sanFilePathBase =
            "C:\\Users\\Zera\\Documents\\LaunchCode Projects\\ScheduleSheets12\\src\\main\\resources\\static\\plaintext\\";

    // Public facing method. Private methods feed individual protocol sanitization back in.
    public static ErrorPackage sanitize(String input) {
        patternInit();
        ErrorPackage output = noError();
        int errCount = 0;
        output = htmlSan(input, output);
        if (output.hasError()) errCount++;
        output = sqlSan(input, output);
        if (output.hasError()) errCount++;
        output = jsSan(input, output);
        if (output.hasError()) errCount++;
        if (errCount > 0) output.addAux("errCount", errCount);
        return output;
    }

    // HTML sanitization - Should find any complete tags, individual markup characters, etc. Substitute markup
    // characters, throw an error if full tags are found (e.g. <script>)
    private static ErrorPackage htmlSan(String input, ErrorPackage output) {
        patternInit();

        // Search and substitute.
        for (Pattern p : patternListHTML) {

        }

        return output;
    }

    // SQL sanitization - should find any command strings and operative characters. Escape as needed - Throw error on
    // anything with a DROP * TABLES command out of spite. TODO - Delete this empty class if it never becomes necessary
    private static ErrorPackage sqlSan(String input, ErrorPackage output) {

        return output;
    }

    // JavaScript sanitization - Should find any script command characters, and substitute them.
    // TODO - Delete this empty class if it never becomes necessary
    private static ErrorPackage jsSan(String input, ErrorPackage output) {

        return output;
    }

    // Initialize pattern ArrayList
    // TODO - make this read from a file or database entries
    private static void patternInit() {
        boolean[] timesSame = timeSame();
        boolean allTimeSame = true;

        for (boolean b : timesSame) {
            allTimeSame = b;
            if (!allTimeSame) break;
        }

        if (plInit && allTimeSame) return;
        try {
            // buffer lists
            List<String> plHStr = Files.readAllLines(Paths.get(sanFilePathBase + "SanPatHTML.txt"));
            List<String> plSStr = Files.readAllLines(Paths.get(sanFilePathBase + "SanPatSQL.txt"));
            List<String> plJStr = Files.readAllLines(Paths.get(sanFilePathBase + "SanPatJS.txt"));

            patternListHTML = Collections.emptyList();
            patternListSQL = Collections.emptyList();
            patternListJS = Collections.emptyList();

            // Convert/Import buffer
            for (String s : plHStr) parseOut(patternListHTML, s);
            for (String s : plSStr) parseOut(patternListSQL, s);
            for (String s : plJStr) parseOut(patternListJS, s);

            plInit = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Checks for file time change
    private static boolean[] timeSame() {
        try {
            FileTime currentTime = Files.getLastModifiedTime(Paths.get(sanFilePathBase + "SanPatHTML.txt"));
            if (currentTime.compareTo(lastMod) != 0) {
                return new boolean[] {true, true, true};
            } else return new boolean[] {false, false, false};
        } catch (IOException e) {
            return new boolean[]{false, false, false};
        }
    }

    // Parse out .txt comments
    private static void parseOut(List patLis, String regexLine) {
        boolean comment = commentMarker.matcher(regexLine).lookingAt();
        if (comment) return;
        patLis.add(Pattern.compile(regexLine));
    }
}
