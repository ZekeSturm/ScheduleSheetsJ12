package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

import static java.util.Calendar.*;
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

    // Pair UIDs with their Participant ID - returns false if failed
    public static boolean idPairing(int pID, int uID) {
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

    // Check login status. If transfer is set to true, will refresh an unexpired session if user is logged in
    public static LoginPackage checkLog(HttpServletRequest request, HttpServletResponse response, boolean transfer) {

        // Get time-of-request
        Calendar currentTime = getInstance();

        // Fetch session
        HttpSession session = request.getSession();

        // Setup things to check for attributes
        boolean userNameFound = false;
        boolean userIDFound = false;
        boolean userKeyFound = false;
        boolean sessAgeFound = false;
        Enumeration<String> attNames = session.getAttributeNames();

        // Validation code block - break out to error & session clear if it fails at any point

        validation:
        {
            while (attNames.hasMoreElements()) {
                String s = attNames.nextElement();
                if (s.equals("userName")) userNameFound = true;
                if (s.equals("userId")) userIDFound = true;
                if (s.equals("userSKey")) userKeyFound = true;
                if (s.equals("sessionExpiry")) sessAgeFound = true;
            }

            // If any fields are missing, remove the ones that do exist and nullify the session/return default failure LP
            if (!(userNameFound && userIDFound && userKeyFound && sessAgeFound)) break validation;

            // By now all elements must be present - check them for validity, and check if session is expired
            // Start by checking if user exists
            Object uNO = session.getAttribute("userName");
            String userName = null;
            RegUser userByName = null;

            // Check if the userName attribute is indeed a string, and if a user with this username exists
            switch (checkClass(uNO)) {
                case STRING:
                    userName = (String) uNO;
                    for (RegUser u : regUserDao.findAll()) {
                        if (u.getUsername().equals(userName)) {
                            userByName = u;
                            break;
                        }
                    }
                    if (userByName != null) break;
                default:
                    break validation;
            }

            // Next check if there's a user ID, and if it matches the user located.
            Object uIDO = session.getAttribute("userId");
            int userId = -1;
            switch (checkClass(uIDO)) {
                case INTEGER:
                    userId = (int) uIDO;
                    if (userId == userByName.getUID()) break;
                default:
                    break validation;
            }

            // Check if the user key is a hash, and all that jazz
            Object uKO = session.getAttribute("userSKey");
            byte[] userSKey = null;
            switch (checkClass(uKO)) {
                case HASH:
                    userSKey = (byte[]) uKO;
                    if (userByName.checkKey(userSKey)) break;
                default:
                    break validation;
            }

            // Check if the session has expired (also if it has an expiration time)
            // Extend non-expired sessions if transfer is flagged true
            Object sEO = session.getAttribute("sessionExpiry");
            Calendar expiry = null;
            switch (checkClass(sEO)) {
                case CALENDAR:
                    expiry = (Calendar) sEO;
                    if (!currentTime.after(expiry)) break;
                default:
                    break validation;
            }
            if (transfer) expiry.set(MINUTE, currentTime.get(MINUTE) + 5);

            session.setAttribute("sessionExpiry", expiry);

            return new LoginPackage(true, userSKey, session, request, response);
        }
        // Only called if breaking out of the above validation block if something has gone wrong
        session = clearUserFields(session);
        return new LoginPackage(session);
    }

    // Same as above - assumes transfer = true
    public static LoginPackage checkLog(HttpServletRequest request, HttpServletResponse response) {
        return checkLog(request, response, true); }

    // Check login status. Do not automatically forward information or extend login duration
    public static LoginPackage checkLog(HttpServletRequest request) { return checkLog(request, null, false); }

    private static HttpSession clearUserFields(HttpSession session) {
        Enumeration<String> attNames = session.getAttributeNames();
        while (attNames.hasMoreElements()) {
            String s = attNames.nextElement();
            if (s.equals("userName")) session.removeAttribute("userName");
            if (s.equals("userId")) session.removeAttribute("userId");
            if (s.equals("userSKey")) session.removeAttribute("userSKey");
            if (s.equals("sessionExpiry")) session.removeAttribute("sessionExpiry");
        }
        return session;
    }

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
    public static HttpSession handleLogoff(HttpServletRequest request, HttpServletResponse response) {
        // Use checklog to check for the existence of necessary info. This will automatically clear all fields and
        // leave user logged off.
        LoginPackage lP = checkLog(request);
        if (!lP.isLogged()) lP.getSession();

        // Pass cleared session back out
        return clearUserFields(request.getSession());
    }

    // Handle login process
    private static LoginPackage handleLogin
        (Participant p, HttpServletRequest request, HttpServletResponse response, boolean transfer, boolean freshLog) {

        // Save query time
        Calendar qTime = getInstance();

        // Fetch session
        HttpSession session = request.getSession();

        // Check for registration - TODO - Implement TempUser signin on a per-event session basis later
        if (p.registered()) {
            RegUser u = (RegUser) p;
            // Pair IDs
            if (idPairing(u.getID(), u.getUID())) ;
            else return new LoginPackage(handleLogoff(request, response)); // If it somehow bungles return this to be safe

            byte [] uSesKey = new byte[32];

            if (freshLog) {
                // Get session hash
                ErrorPackage handler = u.keyGen();

                if (handler.hasError()) return new LoginPackage(handleLogoff(request, response));

                // Class Check -> clone if true
                if (checkArrayClassThenSet(handler.getAux("key"), uSesKey));
                else return new LoginPackage(handleLogoff(request, response));

                regUserDao.save(u);
            } else {
                Enumeration<String> sesAtts = session.getAttributeNames();

                boolean[] dataFound = {false, false, false, false};

                while (sesAtts.hasMoreElements()) {
                    String s = sesAtts.nextElement();
                    if (s.equals("userName")) dataFound[0] = true;
                    if (s.equals("userId")) dataFound[1] = true;
                    if (s.equals("userSKey")) dataFound[2] = true;
                    if (s.equals("sessionExpiry")) dataFound[3] = true;
                }

                if (!dataFound[0] || !dataFound[1] || !dataFound[2] || !dataFound[3])
                    return new LoginPackage(handleLogoff(request, response));

                // Retrieve session byte/key
                byte[] sesKey = new byte[32];
                if (checkArrayClassThenSet(session.getAttribute("userSKey"), sesKey));
                else return new LoginPackage(handleLogoff(request, response));

                // Check session key against stored user key
                if (!u.checkKey(sesKey)) return new LoginPackage(handleLogoff(request, response));

                // Save byte to uSesKey
                checkArrayClassThenSet(sesKey, uSesKey);
            }

            if (freshLog) {
                session.setAttribute("userName", u.getUsername());
                session.setAttribute("userId", u.getUID());
                session.setAttribute("userSKey", uSesKey);
            }
            if (transfer || freshLog) {
                // Initialize refresh time
                Calendar newExpiry = qTime;
                newExpiry.set(MINUTE, qTime.get(MINUTE) + 5);
                session.setAttribute("sessionExpiry", newExpiry);
            }

            return new LoginPackage(true, uSesKey, session, request, response);

        } else return new LoginPackage(handleLogoff(request, response)); // Return default failure until above to-do is done
    }

    // Shorthand for non-initial login/login check
    private static LoginPackage handleLogin
            (Participant p, HttpServletRequest request, HttpServletResponse response, boolean transfer) {
        return handleLogin (p, request, response, transfer, false); }

    // Shorthand for initial login
    private static LoginPackage handleLogin(Participant p, HttpServletRequest request, HttpServletResponse response) {
        return handleLogin(p, request, response, false, true); }

}
