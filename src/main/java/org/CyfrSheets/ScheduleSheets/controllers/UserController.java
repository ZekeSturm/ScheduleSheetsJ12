package org.CyfrSheets.ScheduleSheets.controllers;

import org.CyfrSheets.ScheduleSheets.models.data.BaseEventDao;
import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidPasswordException;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

@Controller
@RequestMapping(value = "user")
public class UserController {

    @Autowired
    private RegUserDao regUserDao;

    @Autowired
    private ParticipantDao participantDao;

    @Autowired
    private BaseEventDao baseEventDao;

    @GetMapping(value = "/")
    public String index(Model model, HttpSession session) {

        model.addAttribute("sessionId", session.getId());

        boolean logged = checkLog(session);

        model.addAttribute("logged", logged);

        if (logged) {
            // fetch user ID from session
            int uID = (int)session.getAttribute("userId");
            String username = ((Cookie)session.getAttribute("userCookie")).getName();
            model.addAttribute("title", username + "'s proflie");
            return "redirect:" + uID;
        } else {
            model.addAttribute("title", "Register a new account");
            session = clearUser(session);
            return "redirect:register";
        }

    }

    @GetMapping(value="{uid}")
    public String profile(Model model, HttpSession session, @PathVariable("uid") String uIDstr) {

        model.addAttribute("sessionId", session.getId());

        ErrorPackage handler = parseNextInt(uIDstr);

        // Is the path variable actually an integer?
        if (handler.hasError()) {
            model.addAttribute("invaliduid", true);
            return "user/profile";
        }

        int uID = (int)handler.getAux("intOut");

        boolean logged = checkLog(session);

        model.addAttribute("logged", logged);

        RegUser u = regUserDao.findById(uIDtopID(uID)).get();

        // Is there a user with this ID?
        if (u == null) {
            model.addAttribute("uidnonmatch", true);
            return "user/profile";
        }

        model.addAttribute("user", u);

        if (logged) {
            model.addAttribute("title", "Your Profile");
            model.addAttribute("personal", true);
        } else {
            model.addAttribute("title", u.getUsername() + "'s Profile");
            model.addAttribute("personal", false);
        }

        return "user/profile";
    }

    @GetMapping(value = "login")
    public String login(Model model, HttpSession session) {

        model.addAttribute("sessionId", session.getId());

        boolean logged = checkLog(session);

        if (logged) {
            // redirect to user profile
            int uID = (int)session.getAttribute("userId");
            return "redirect:/user/" + uID;
        } else {
            // To the login page
            model.addAttribute("title", "Log In!");
            return "user/login";
        }
    }

    @PostMapping(value = "login")
    public String login(Model model, HttpServletRequest request, @RequestParam("username") String username, @RequestParam("password") String password) {

        // Check for being somehow already logged in
        boolean logged = checkLog(request.getSession());

        if (logged) {
            // redirect to user profile
            int uID = (int)request.getSession().getAttribute("userId");
            return "redirect:/user/" + uID;
        } else request = clearUserReq(request); // cautionary data clear

        RegUser target = null;

        for (RegUser u : regUserDao.findAll()) {
            if (u.getUsername().toLowerCase().equals(username)) target = u;
        }

        // Username not found
        if (target == null) {
            model.addAttribute("invaliduserpass", true);
            model.addAttribute("title", "Log In!");
            return "redirect:";
        }

        // Password check
        ErrorPackage handler = target.checkPassword(password);

        // Handle inconceivable no such algorithm exceptions
        if (handler.hasError()) {
            model.addAttribute("genericerror", true);
            return "redirect:";
        }

        // Unwrap password check
        if ((boolean)handler.getAux("ancil")) {
            // success - initiate session
            ErrorPackage keyHandler = target.keyGen(password); // keyHandler should not be able to have an error at this stage

            request = userSessionInitReq(request, target, (byte[])keyHandler.getAux("sKey"));
            return "redirect:/user/" + target.getUID();

        } else {
            // failure - back you go
            model.addAttribute("invaliduserpass", true);
            model.addAttribute("title", "Log In!");
            return "redirect:";
        }
    }

    @GetMapping(value = "logoff")
    public String logOff(Model model, HttpSession session) {

        boolean logged = checkLog(session);

        if (!logged) return "redirect:/user";

        model.addAttribute("sessionId", session.getId());
        model.addAttribute("title", "Log Off");

        return "user/logoff";
    }

    @PostMapping(value = "logoff")
    public String logOff(Model model, HttpServletRequest request, @RequestParam("out") String outStr) {

        boolean out = parseBool(outStr);

        if (out) {
            // Log Off
            request = clearUserReq(request);
            return "redirect:/";
        } else {
            // Redirect to userpage
            return "redirect:/user/" + (int)request.getSession().getAttribute("userId");
        }
    }

    @GetMapping(value = "register")
    public String register(Model model, HttpSession session) {

        model.addAttribute("sessionId", session.getId());

        boolean logged = checkLog(session);

        if (logged) {
            // redirect to user profile
            int uID = (int)session.getAttribute("userId");
            return "redirect:/user/" + uID;
        } else {
            // Create new user
            model.addAttribute("title", "Register a new account");
            session = clearUser(session);
            return "user/register";
        }
    }

    @PostMapping(value = "register")
    public String register(Model model, HttpServletRequest request, @RequestParam("username") String username,
                           @RequestParam("password") String password, @RequestParam("confirm") String confirm,
                           @RequestParam("email") String email) {

        // Input error checking first
        boolean logged = checkLog(request.getSession());

        // Check if already logged in
        if (logged) {
            // redirect to user profile
            int uID = (int)request.getSession().getAttribute("userId");
            return "redirect:/user/" + uID;
        } else request = clearUserReq(request); // Clear user data just in case

        // Check for username
        if (username.isEmpty()) {
            model.addAttribute("missingusername", true);
            return "redirect:/user/register";
        }

        // Check for password/confirm
        if (password.isEmpty() || confirm.isEmpty()) {
            model.addAttribute("missingpass", true);
            return "redirect:/user/register";
        }

        // If passwords do not match, return to register page
        if (!password.equals(confirm)) {
            model.addAttribute("passmismatch", true);
            return "redirect:/user/register";
        }

        // Must have email
        if (email.isEmpty()) {
            model.addAttribute("missingemail", true);
            return "redirect:/user/register";
        }

        // Check if username or email is already taken
        for (RegUser u: regUserDao.findAll()) {
            if (u.getUsername().equals(username)) {
                model.addAttribute("nametaken", true);
                return "redirect:/user/register";
            }
            if (u.getEmail().equals(email)) {
                model.addAttribute("emailused", true);
                return "redirect:/user/register";
            }
        }

        try {
            // Create new user (or attempt to)
            RegUser newU = new RegUser(username, password, email);

            regUserDao.save(newU);
            participantDao.save(newU);

            ErrorPackage handler = newU.keyGen(password); // TODO - Use the key from this for authentication across app

            if (handler.hasError()) {
                if (handler.getMessage().equals("Password Mismatch")) {
                    model.addAttribute("passmismatch", true);
                    return "redirect:/user/register";
                }
                model.addAttribute("genericerror", true);
                return "redirect:/user/register";
            }

            request = userSessionInitReq(request, newU, (byte[])handler.getAux("sKey"));

            return "redirect:/user/" + newU.getUID();
        } catch (InvalidPasswordException e) {
            model.addAttribute("passmismatch", true);
            // Clear user data for security
            request = clearUserReq(request);
            return "redirect:/user/register";
        }
    }

    // Use anytime login status is relevant... so likely on every page that has a navbar
    public static boolean checkLog(HttpSession session) {
        Enumeration<String> sesNameEnum = session.getAttributeNames();
        while (sesNameEnum.hasMoreElements()) if (sesNameEnum.nextElement().equals("userCookie")) return true;
        return false;
    }

    // Anytime the above returns false, run this
    protected static HttpSession clearUser(HttpSession session) {
        Enumeration<String> sNE = session.getAttributeNames();
        while (sNE.hasMoreElements()) {
            String next = sNE.nextElement();
            if (next.equals("userCookie") || next.equals("userKey") || next.equals("userId"))
                session.removeAttribute(next);
        }
        return session;
    }

    // Same, but for sessions within ServletRequests
    protected static HttpServletRequest clearUserReq(HttpServletRequest request) {
        Enumeration<String> sNE = request.getSession().getAttributeNames();
        while (sNE.hasMoreElements()) {
            String next = sNE.nextElement();
            if (next.equals("userCookie") || next.equals("userKey") || next.equals("userId"))
                request.getSession().removeAttribute(next);
        }
        return request;
    }

    private int uIDtopID(int uID) {
        for (RegUser u : regUserDao.findAll()) if (u.getUID() == uID) return u.getID();
        return -1;
    }

    // Initialize new user session
    private HttpSession userSessionInit(HttpSession session, RegUser u, byte[] key) {

        // Clear any old data
        session = clearUser(session);

        // Create user login cookie. Carries minimal data - main purpose is for timing out the session data
        Cookie userCookie = new Cookie("username", u.getUsername());
        // User data timeout: 10 minutes
        userCookie.setMaxAge(600);
        // Bad javascript injections are bad
        userCookie.setHttpOnly(true);

        session.setAttribute("userCookie", userCookie);
        session.setAttribute("userKey", key);
        session.setAttribute("userId", u.getUID());
        return session;
    }

    // See above, but handles servlet request sessions
    private HttpServletRequest userSessionInitReq(HttpServletRequest request, RegUser u, byte[] key) {

        // Clear any old data
        request = clearUserReq(request);

        // Create user login cookie. Carries minimal data - main purpose is for timing out the session data
        Cookie userCookie = new Cookie("username", u.getUsername());
        // User data timeout: 10 minutes
        userCookie.setMaxAge(600);
        // Bad javascript injections are bad
        userCookie.setHttpOnly(true);

        request.getSession().setAttribute("userCookie", userCookie);
        request.getSession().setAttribute("userKey", key);
        request.getSession().setAttribute("userId", u.getUID());
        return request;
    }
}
