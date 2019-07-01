package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.CyfrSheets.ScheduleSheets.models.utilities.LoginPackage.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassCase.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.*;

@Component
public class LoginUtil {

    // Autowired DAO candidates and the static objects they are initialized into
    @Autowired
    private ParticipantDao pD;
    private static ParticipantDao participantDao;

    @Autowired
    private RegUserDao ruD;
    private static RegUserDao regUserDao;

    private static HashMap<Integer, Integer> idMap = new HashMap<>(); // uID/pID pairing map - uID key, pID value

    // Constructor literally only used for the below. Should probably make it private. - Did it, past me. - current me
    private LoginUtil () { }

    // Call this before every method using the DAO - Allows "Autowiring" into a static variable
    private static void daoInit () {
        LoginUtil lu = new LoginUtil();
        participantDao = lu.pD;
        regUserDao = lu.ruD;
    }

    // Handle login
    // TODO - Implement. Pass keyGen output to session along w/ uID & make cookie w/ relevant info
    // (REMOVE THIS WITH ABOVE TO DO) Cookie Format: (uID + "checkbyte" + pID, parsed string of keyGen byte)
    public static LoginPackage handleLogin (Participant p, HttpServletRequest request, HttpServletResponse response) {

        // Check for registration - TODO - Implement TempUser signin on a per-event session basis later
        if (p.registered()) {
            RegUser u = (RegUser)p;
            // Pair IDs
            if(idPairing(u.getID(), u.getUID()));
            else return new LoginPackage(); // If it somehow bungles return this to be safe

            ErrorPackage handler = u.keyGen();

            if (handler.hasError()) return new LoginPackage();



        } else return new LoginPackage(); // Return default failure until above to-do is done

        // Junk Return - remove when implemented
        return new LoginPackage();
    }

    // Pair UIDs with their Participant ID - returns false if failed
    public static boolean idPairing (int pID, int uID) {
        // Check for existence of user w/ id
        Optional<RegUser> o = regUserDao.findById(pID);
        if (o.isPresent()) {
            RegUser u = o.get();
            // Confirm UID and ID are paired
            if (u.getID() == pID && u.getUID() == uID) {
                idMap.put(uID, pID);
                return true;
            }
        }
        return false;
    }

    // Check login status. If logged in (and transfer == true), extend duration of cookie/transfer information directly
    // to the response
    public static LoginPackage checkLog(HttpServletRequest request, HttpServletResponse response, boolean transfer) {

        // Fetch session & cookies
        HttpSession session = request.getSession();
        Cookie[] cookies = request.getCookies();

        // Persisting variable to check for logged user cookie
        boolean logCookieFound = false;
        Cookie logCookie = null;

        // Check for logged user cookie
        if (cookies.length != 0) for (Cookie c : cookies) if (c.getName().toLowerCase().contains("checkbyte")) {
            logCookie = c;
            logCookieFound = true;
        } else if (logCookieFound) break;

        if (logCookieFound) { // Return logged in - transfer data if transfer == true
            // Cookie name format - uID + "checkbyte" + pID]
            LoginPackage out;
            ErrorPackage handler = parseInts(logCookie.getName());

            if (handler.hasError()) return new LoginPackage(); // Empty failure default - no id info found

            // Extract arraylist - check to ensure the right amount of ints were found
            ArrayList<Integer> ids = (ArrayList<Integer>)handler.getAux("arrayOut");
            if (ids.size() < 2 || ids.size() > 2) return new LoginPackage();

            // Extract IDs
            int uID = ids.get(0);
            int pID = ids.get(1);

            // Make sure uID and pID match
            if (idMap.get(uID) != pID) return new LoginPackage();

            // Make sure user exists - pull user session key, check against cookie key & session key
            Optional<RegUser> possibleUser = regUserDao.findById(pID);
            if (!possibleUser.isPresent()) return new LoginPackage();

            RegUser ru = possibleUser.get();

            if (!ru.checkKey(logCookie.getValue())) return new LoginPackage();




            if (transfer) {

            } else {


            }
            // return out;
            // Above line commented out to avoid error throwing - un-comment when implemented
        } else return new LoginPackage(); // Return a "false"/no-login LoginPackage. Empty constructor is failure-default.

        // Junk return. Remove when implemented
        return new LoginPackage();
    }

    // Same as above - assumes transfer = true
    public static LoginPackage checkLog(HttpServletRequest request, HttpServletResponse response) { return
            checkLog(request, response, false); }

    // Check login status. Do not automatically forward information or extend login duration
    public static LoginPackage checkLog(HttpServletRequest request) { return
            checkLog(request, null, false); }

    // Simplify finding user by UID - returns null on failure
    public static RegUser findUserByUID(int uID) {
        try { if (idMap.containsKey(uID)) return regUserDao.findById(idMap.get(uID)).get(); }
        catch (NoSuchElementException e) { } // Exception swatter
        return null;
    }
}
