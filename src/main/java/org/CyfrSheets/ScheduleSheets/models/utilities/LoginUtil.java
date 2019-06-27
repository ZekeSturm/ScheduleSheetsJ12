package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.CyfrSheets.ScheduleSheets.models.utilities.LoginPackage.*;

@Component
public class LoginUtil {

    // Autowired DAO candidates and the static objects they are initialized into
    @Autowired
    private ParticipantDao pD;
    private static ParticipantDao participantDao;

    @Autowired
    private RegUserDao ruD;
    private static RegUserDao regUserDao;

    private static HashMap<Integer, Integer> idMap = new HashMap<>();

    // Constructor literally only used for the below. Should probably make it private. - Did it, past me. - current me
    private LoginUtil () { }

    // Call this before every method using the DAO - Allows "Autowiring" into a static variable
    private static void daoInit () {
        LoginUtil lu = new LoginUtil();
        participantDao = lu.pD;
        regUserDao = lu.ruD;
    }

    // Handle login
    public static LoginPackage handleLogin (HttpServletRequest request, HttpServletResponse response) {


    }

    // Pair UIDs with their Participant ID - returns false if failed
    public static boolean idPairing (int id, int uID) {
        // Check for existence of user w/ id
        Optional<RegUser> o = regUserDao.findById(id);
        if (o.isPresent()) {
            RegUser u = o.get();
            // Confirm UID and ID are paired
            if (u.getID() == id && u.getUID() == uID) {
                idMap.put(uID, id);
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

        } else { // Return a "false"/no-login LoginPackage. Empty constructor is failure-default.
            return new LoginPackage();
        }
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
