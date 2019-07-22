package org.CyfrSheets.ScheduleSheets.controllers;

import org.CyfrSheets.ScheduleSheets.models.data.BaseEventDao;
import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidPasswordException;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;
import org.CyfrSheets.ScheduleSheets.models.utilities.LoginPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.checkClassThenSet;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ExtraUtil.getUserWithID;
import static org.CyfrSheets.ScheduleSheets.models.utilities.LoginUtil.*;
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

    @GetMapping(value = {"", "/"})
    public String index(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response) {

        LoginPackage lP = checkLog(request, response);

        boolean logged = lP.isLogged();

        model.addAttribute("logged", logged);

        if (logged) {
            // fetch user ID from session
            int uID = (int) session.getAttribute("userId");
            model.addAttribute("title", "Your profile");
            return "redirect:user/profile/" + uID;
        } else {
            model.addAttribute("title", "Register a new account");
            session = handleLogoff(request, response);
            return "redirect:/user/register";
        }

    }

    @GetMapping(value = "/profile/{uid}")
    public String profile(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response, @PathVariable("uid") String uIDstr) {

        // Pull int out of uid string
        ErrorPackage handler = parseSingleInt(uIDstr, true);

        // Is the path variable actually an integer?
        if (handler.hasError()) {
            model.addAttribute("invaliduid", true);
            return "user/profile";
        }

        // int uID = (int) handler.getAux("intOut"); - Old cast method
        // New error check method below
        int uID = -2147483648;

        Object[] inOut = {handler.getAux("intOut"), uID};

        if (checkClassThenSet(inOut));

        else { model.addAttribute("genericerror", true);
            return "user/profile"; }

        uID = (int)inOut[1];

        boolean logged = checkLog(request, response).isLogged();

        model.addAttribute("logged", logged);

        RegUser u = findUserByUID(uID);

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
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession();

        boolean logged = checkLog(request, response).isLogged();

        if (logged) {
            // redirect to user profile
            int uID = (int) session.getAttribute("userId");
            return "redirect:/user/profile/" + uID;
        } else {
            // To the login page
            model.addAttribute("title", "Log In!");
            return "user/login";
        }
    }

    @PostMapping(value = "login")
    public String login(Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username, @RequestParam("password") String password) {

        // Check for being somehow already logged in
        LoginPackage lP = checkLog(request, response);
        boolean logged = lP.isLogged();

        // Initialize session
        HttpSession session = request.getSession();

        if (logged) {
            // redirect to user profile
            int uID = (int) request.getSession().getAttribute("userId");
            return "redirect:/user/" + uID;
        } else session = handleLogoff(request, response); // cautionary data clear

        RegUser target = null;

        for (RegUser u : regUserDao.findAll()) {
            if (u.getUsername().toLowerCase().equals(username.toLowerCase())) target = u;
        }

        // Username not found
        if (target == null) {
            model.addAttribute("invaliduserpass", true);
            model.addAttribute("title", "Log In!");
            return "redirect:";
        }

        // Handle login
        LoginPackage logPack = handleLoginSecurity(target, password, request, response);

        // Unwrap login check/package
        if (logPack.badPackage() || !logPack.isLogged()) {
            // Failure - Back you go
            model.addAttribute("invaliduserpass", true);
            model.addAttribute("title", "Log In!");
            return "redirect:";
        }
        // Otherwise, success - session should be mostly initiated already by LoginUtil
        return "redirect:/user/profile/" + target.getUID();
    }

    @GetMapping(value = "logoff")
    public String logOff(Model model, HttpServletRequest request, HttpServletResponse response) {
        // Fetch session
        HttpSession session = request.getSession();

        boolean logged = checkLog(request, response).isLogged();

        if (!logged) return "redirect:/user/login";

        model.addAttribute("userId", session.getAttribute("userId"));
        model.addAttribute("title", "Log Off");

        return "user/logoff";
    }

    // Only called if submit post button is pressed. get button should redirect to user profile
    @PostMapping(value = "logoff")
    public String logOff(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession();

        session = handleLogoff(request, response);
        return "redirect:/";
    }

    @GetMapping(value = "register")
    public String register(Model model, HttpServletRequest request, HttpServletResponse response) {

        // Fetch session
        HttpSession session = request.getSession();

        boolean logged = checkLog(request, response).isLogged();

        if (logged) {
            // redirect to user profile
            int uID = (int) session.getAttribute("userId");
            return "redirect:/user/" + uID;
        } else {
            // Create new user
            model.addAttribute("title", "Register a new account");
            session = handleLogoff(request, response);
            return "user/register";
        }
    }

    @PostMapping(value = "register")
    public String register(Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username,
                           @RequestParam("password") String password, @RequestParam("confirm") String confirm,
                           @RequestParam("email") String email) {

        // Check if already logged in
        boolean logged = checkLog(request, response).isLogged();

        HttpSession session = request.getSession();

        if (logged) {
            // redirect to user profile
            int uID = (int) request.getSession().getAttribute("userId");
            return "redirect:/user/" + uID;
        } else session = handleLogoff(request, response); // Clear user data just in case

        // Check for username
        if (username.isEmpty()) {
            model.addAttribute("missingusername", true);
            return "redirect:/user/register";
        }

        // Check for password/confirm
        if (password.isEmpty()) {
            model.addAttribute("missingpass", true);
            return "redirect:/user/register";
        }

        // If passwords do not match, return to register page
        if (confirm.isEmpty() || !password.equals(confirm)) {
            model.addAttribute("passmismatch", true);
            return "redirect:/user/register";
        }

        // Must have email
        if (email.isEmpty()) {
            model.addAttribute("missingemail", true);
            return "redirect:/user/register";
        }

        // Check if username or email is already taken
        for (RegUser u : regUserDao.findAll()) {
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
            RegUser newU = getUserWithID(username, password, email);

            regUserDao.save(newU);
            participantDao.save(newU);

            handleLoginSecurity(newU, password, request, response);

            return "redirect:/user/profile/" + newU.getID();
        } catch (InvalidPasswordException e) {
            model.addAttribute("passmismatch", true);
            // Clear user data for security
            session = handleLogoff(request, response);
            return "redirect:/user/register";
        }
    }

}