package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;
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

    private static boolean daoInit = false;

    private static HashMap<Integer, Integer> idMap = new HashMap<>(); // uID/pID pairing map - uID key, pID value

    // Constructor literally only used for the below. Should probably make it private. - Did it, past me. - current me
    private LoginUtil() {
    }

    @PostConstruct
    private void daoInit() {
        if (!daoInit) {
            regUserDao = this.ruD;
            participantDao = this.pD;
            if (regUserDao != null && participantDao != null) daoInit = true;
            else daoInit = false;
        }
    }

    /**
    // Call this before every method using the DAO - Allows "Autowiring" into a static variable
    private static void daoInit() {
        if (regUserDao == null || participantDao == null) daoInit = false;
        if (!daoInit) {
            regUserDao = this.ruD;
        }
    } */


    // Pair UIDs with their Participant ID - returns false if failed
    public static boolean idPairing(int pID, int uID) {
        // DAO thingy
        // daoInit();
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

    // Public facing handle login - validates before passing to the main handler
    public static LoginPackage handleLoginSecurity(Participant p, String pass, HttpServletRequest request, HttpServletResponse response) {
        ErrorPackage handler = p.checkPassword(pass);
        if (handler.hasError() || !handler.getAncil()) return new LoginPackage();

        return handleLogin(p, request, response); // Fresh login
    }

    // Check login status. If transfer is set to true, will send a fresh version of the cookie if the user is logged in
    public static LoginPackage checkLog(HttpServletRequest request, HttpServletResponse response, boolean transfer) {
        // DAO thingy
        // daoInit();

        // Fetch cookies
        Cookie[] cookies = request.getCookies();

        // Persisting variable to check for logged user cookie
        boolean logCookieFound = false;
        Cookie logCookie = null;

        // Check for logged user cookie
        if (cookies.length != 0) for (Cookie c : cookies)
            if (c.getName().toLowerCase().contains("checkbyte")) {
                logCookie = c;
                logCookieFound = true;
            } else if (logCookieFound) break;

        if (logCookieFound) { // Return logged in - transfer data if transfer == true
            // Cookie name format - uID + "checkbyte" + pID]
            LoginPackage out;
            ErrorPackage handler = parseInts(logCookie.getName());

            if (handler.hasError()) return new LoginPackage(); // Empty failure default - no id info found

            // Extract arraylist - check to ensure the right amount of ints were found
            ArrayList<Integer> ids = (ArrayList<Integer>) handler.getAux("arrayOut");
            if (ids.size() < 2 || ids.size() > 2) return new LoginPackage();

            // Extract IDs
            int uID = ids.get(0);
            int pID = ids.get(1);

            boolean hasUID = idMap.containsKey(uID);

            // Make sure uID and pID match
            if (!hasUID) hasUID = idPairing(pID, uID);
            if (!hasUID) return new LoginPackage();
            if(idMap.get(uID) != pID) return new LoginPackage();

            // Make sure user exists - pull user session key, check against cookie key & session key
            Optional<RegUser> possibleUser = regUserDao.findById(pID);
            if (!possibleUser.isPresent()) return new LoginPackage();

            RegUser ru = possibleUser.get();

            if (!ru.checkKey(logCookie.getValue())) return new LoginPackage();

            return handleLogin(ru, request, response, transfer);
        } else
            return new LoginPackage(false, null, request, response); // Login failure - No error along the way
    }

    // Same as above - assumes transfer = true
    public static LoginPackage checkLog(HttpServletRequest request, HttpServletResponse response) {
        return checkLog(request, response, true); }

    // Check login status. Do not automatically forward information or extend login duration
    public static LoginPackage checkLog(HttpServletRequest request) { return checkLog(request, null, false); }

    // Simplify finding user by UID - returns null on failure
    public static RegUser findUserByUID(int uID) {
        try {
            if (idMap.containsKey(uID)) return regUserDao.findById(idMap.get(uID)).get();
        } catch (NoSuchElementException e) { } // Exception swatter
        return null;
    }

    // Above, but usable by outside RegUserDao
    public static int findPIDByUID(int uID) {
        if (idMap.containsKey(uID)) return idMap.get(uID);
        return -2147483648; // Return largest -int to check for errors
    }

    // What it says on the tin
    public static boolean handleLogoff(HttpServletRequest request, HttpServletResponse response) {
        // Fetch cookies - check for login cookie
        Cookie[] cookies = request.getCookies();
        Cookie logC = null;
        for (Cookie c : cookies) if (c.getName().contains("checkbyte")) logC = c;

        // Fetch attribute names from session
        Enumeration<String> sNE = request.getSession().getAttributeNames();
        // Find login attributes
        boolean nameF = false;
        boolean keyF = false;
        boolean idF = false;

        while (sNE.hasMoreElements()) {
            String next = sNE.nextElement();
            switch (next) {
                case "userName":
                    nameF = true;
                    continue;
                case "userSKey":
                    keyF = true;
                    continue;
                case "userId":
                    idF = true;
                    continue;
                default:
                    continue;
            }
        }

        if (logC == null && !nameF && !keyF && !idF) return true;

        if (logC != null && !checkByteAgainstString((byte[])request.getSession().getAttribute("userSKey"), logC.getValue()))
            return false;
        // Wrong log cookie for this session. somehow. TODO - add some debug for this later

        // Wipe found fields
        if (nameF) request.getSession().removeAttribute("userName");
        if (keyF) request.getSession().removeAttribute("userSKey");
        if (idF) request.getSession().removeAttribute("userId");

        if (logC != null) {
            logC.setMaxAge(0);
            logC.setPath("/");
            response.addCookie(logC);
        }

        return true;
    }

    // Handle login process
    private static LoginPackage handleLogin
        (Participant p, HttpServletRequest request, HttpServletResponse response, boolean transfer, boolean freshLog) {

        // Fetch session
        HttpSession session = request.getSession();

        // Check for registration - TODO - Implement TempUser signin on a per-event session basis later
        if (p.registered()) {
            RegUser u = (RegUser) p;
            // Pair IDs
            if (idPairing(u.getID(), u.getUID())) ;
            else return new LoginPackage(); // If it somehow bungles return this to be safe

            byte [] uSesKey = new byte[32];

            Cookie fresh = null;

            if (freshLog) {
                // Get session hash
                ErrorPackage handler = u.keyGen();

                if (handler.hasError()) return new LoginPackage();

                Object[] inOut = {handler.getAux("key"), uSesKey};

                // Class Check -> clone if true
                if (checkClassThenSet(inOut));
                else return new LoginPackage();

                regUserDao.save(u);

                // Cookie init
                fresh = new Cookie(u.getUID() + "checkbyte" + u.getID(), parseByteToString(uSesKey));
                fresh.setMaxAge(600);
                fresh.setHttpOnly(true);
                fresh.setPath("/");
                // Future me: If you figure out how to run this https - setSecure(true) here - Sincerely, past you
            } else {
                // Grab cookies to check for login cookie
                Cookie[] cookies = request.getCookies();

                // Check cookies for "checkbyte" cookie and compare string-key to user session key. Make "fresh" cookie
                // match this cookie if found - if not found, will remain null
                for (Cookie c : cookies)
                    if (c.getName().equals(u.getUID() + "checkbyte" + u.getID()) && u.checkKey(c.getValue()))
                        fresh = c;

                if (fresh == null) return new LoginPackage(); // Failure to fetch login cookie - probably not there

                Enumeration<String> sesAtts = session.getAttributeNames();

                boolean[] dataFound = {false, false, false};

                while (sesAtts.hasMoreElements()) {
                    String s = sesAtts.nextElement();
                    if (s.equals("userName")) dataFound[0] = true;
                    if (s.equals("userId")) dataFound[1] = true;
                    if (s.equals("userSKey")) dataFound[2] = true;
                }

                if (!dataFound[0] || !dataFound[1] || !dataFound[2]) return new LoginPackage();

                // Retrieve session byte/key
                byte[] sesKey = new byte[32];
                if (checkArrayClassThenSet(session.getAttribute("userSKey"), sesKey));
                else return new LoginPackage();

                // Check cookie byte string against session byte
                if (!checkByteAgainstString(sesKey, fresh.getValue())) return new LoginPackage();

                // Save byte to uSesKey
                checkArrayClassThenSet(sesKey, uSesKey);
            }

            if (transfer || freshLog) response.addCookie(fresh);
            if (freshLog) {
                session.setAttribute("userName", u.getUsername());
                session.setAttribute("userId", u.getUID());
                session.setAttribute("userSKey", uSesKey);
            }

            return new LoginPackage(true, uSesKey, request, response);

        } else return new LoginPackage(); // Return default failure until above to-do is done
    }

    // Shorthand for non-initial login/login check
    private static LoginPackage handleLogin
            (Participant p, HttpServletRequest request, HttpServletResponse response, boolean transfer) {
        return handleLogin (p, request, response, transfer, false); }

    // Shorthand for initial login
    private static LoginPackage handleLogin(Participant p, HttpServletRequest request, HttpServletResponse response) {
        return handleLogin(p, request, response, false, true); }

}
