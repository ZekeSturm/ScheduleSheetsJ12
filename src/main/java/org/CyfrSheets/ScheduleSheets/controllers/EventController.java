package org.CyfrSheets.ScheduleSheets.controllers;

import org.CyfrSheets.ScheduleSheets.models.data.*;
import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;
import org.CyfrSheets.ScheduleSheets.models.events.StaticEvent;
import org.CyfrSheets.ScheduleSheets.models.forms.StaticEventForm;
import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.CyfrSheets.ScheduleSheets.models.users.TempUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import static org.CyfrSheets.ScheduleSheets.models.events.StaticEvent.seInit;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.LoginUtil.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

@Controller
@RequestMapping(value = "event")
public class EventController {

    /** Controller for all events - TempUsers also handled here given they are event-unique */

    @Autowired
    BaseEventDao baseEventDao;

    @Autowired
    StaticEventDao staticEventDao;

    @Autowired
    ParticipantDao participantDao;

    @Autowired
    TempUserDao tempUserDao;

    @Autowired
    RegUserDao regUserDao;

    @GetMapping(value = "")
    public String eventHub(Model model, HttpServletRequest request, HttpServletResponse response) {

        boolean logged = checkLog(request, response).isLogged();

        if (!logged) handleLogoff(request, response);

        model.addAttribute("title", "Events");

        return "event/index";
    }

    @GetMapping(value = "new/static")
    public String newStaticEvent(Model model, HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession();

        boolean logged = checkLog(request, response).isLogged();
        boolean tLogged = checkTLog(session);

        if (!logged) handleLogoff(request, response);

        model.addAttribute("logged", logged);
        model.addAttribute("tLogged", tLogged);

        model.addAttribute("badpass", false);

        StaticEventForm form;

        if (session.getAttribute("form") != null) form = (StaticEventForm)session.getAttribute("form"); // Make a class check for this later
        else form = new StaticEventForm(logged);

        model.addAttribute("form", form);

        model.addAttribute("title", "Create your new event!");

        return "event/new-static";
    }

    @PostMapping(value = "new/static")
    public String newStaticEvent(Model model, HttpServletRequest request, HttpServletResponse response,
                                 @ModelAttribute("form") StaticEventForm form) {

        boolean logged = checkLog(request, response).isLogged();

        // Fetch which fields are and are not present
        ArrayList<String> whatsMissing = form.whatsMissing();

        // Check if any fields are missing - will be used to toss back if necessary and skip unnecessary blocks of code
        boolean missingBits = whatsMissing.size() > 0;
        // boolean missingLogin = false;

        // Temp Login for unregistered users
        if (!logged) {
            // Briefly hijack first logged check to clear user data just in case
            handleLogoff(request, response);

            // Confirm all three fields exist before checking password
            if (missingBits) {
                if (whatsMissing.contains("cName")) model.addAttribute("noCName", true);
                if (whatsMissing.contains("cPass")) model.addAttribute("noCPass", true);
                if (whatsMissing.contains("pConfirm")) model.addAttribute("noConfirm", true);
            }

            // Confirm passwords against each other
            if (!form.passMatch()) {
                model.addAttribute("form", form);
                model.addAttribute("badpass", true);
                model.addAttribute("title", "Create your new event!");
                return "redirect:/event/new/static";
            }
        }

        // Post-login, check all other missing fields before proceeding - some are permissible

        // If fields are missing, check for them
        if (missingBits) {
            // Reset variable - will be used to check for specifically critical missing bits
            missingBits = false;
            for (String s : whatsMissing) {
                // Disregard these fields - already dealt with
                if (equalsAny(s, new String[]{"cName", "cPass", "pConfirm"})) continue;
                // Disregard missing startTime/Date if startCal is initialized
                if (s.contains("start") && form.startData()) continue;
                // Disregard missing endTime/Date if endCal is initialized
                if (s.contains("end") && (form.endData() || !form.hasEndBool())) continue;
                // No valid exception found - add flag to model
                missingBits = true;
                s = "no" + s.substring(0, 1).toUpperCase() + s.substring(1);
            }

            if (missingBits) {
                // Refresh login page if fields are missing - pass along form to retain entered data, save for passwords
                form.setCPass("");
                form.setPConfirm("");

                request.getSession().setAttribute("form", form);
                model.addAttribute("form", form);
                model.addAttribute("title", "Create your new event!");
                return "redirect:/event/new/static";
            }
        }

        StaticEvent out;

        HashMap<String, Object> args = new HashMap<>();

        // Find user and put them in args if logged in
        if (logged) {
            RegUser u = null;

            for (RegUser ru : regUserDao.findAll())
                if (ru.getUID() == (int)request.getSession().getAttribute("userId")) u = ru;

            // Normally I'd check after the above, but it should be impossible to be logged in and missing that data
            // Similarly it should be impossible to delete an active user unless someone jimmy drop tables's me.
            // TODO - On that note, reimplement input sanitizer at some point down the line

            // ... possibly implement null user detection/exception here if it starts giving problems

            args.put("user", u);
        }

        // Pull event name/desc
        String name = form.getEventName();
        String desc = form.getEventDesc();

        // Create event
        if (form.hasEndBool()) out = seInit(name, desc, args, form.getStart(), form.getEnd());
        else out = seInit(name, desc, args, form.getStart());


        // Skip TempUser creation if logged - already fed out a user.
        if (!logged) {
            // TempUser Creation

            // Fetch temp user data.
            String cName = form.getCName();
            String cPass = form.getCPass();

            TempUser tu = new TempUser(cName, cPass, out);

            out.tempInit(tu);

            baseEventDao.save(out);
            staticEventDao.save(out);

            participantDao.save(tu);
            tempUserDao.save(tu);
        }

        baseEventDao.save(out);
        staticEventDao.save(out);


        return "redirect:/event/" + out.getId();
    }

    //--------------------------------------------------------------------------------------------------
    // Two methods below are presently unused. Will be scrapped if modals work anyrate
    //--------------------------------------------------------------------------------------------------

    @GetMapping(value="new/static/temp")
    public String newStaticTempCreator(Model model, HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession();

        boolean logged = false; //checkLog(session);
        boolean tLogged = checkTLog(session);

        model.addAttribute("logged", logged);

        if (logged || tLogged) return "redirect:/event/new/static";
        else handleLogoff(request, response);

        model.addAttribute("sessionId", session.getId());

        model.addAttribute("creating", true);
        model.addAttribute("title", "Register for your event");

        return "event/temp-user-login";
    }

    @PostMapping(value="new/static/temp")
    public String newTempCreator(Model model, HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam("username") String username, @RequestParam("password") String password,
                                 @RequestParam("confirm") String confirm) {

        boolean logged = false; //checkLog(request.getSession());
        boolean tLogged = checkTLog(request.getSession());

        if (logged || tLogged) return "redirect:/event/new/static";
        else handleLogoff(request, response);


        if (password.equals(confirm)) {

            model.addAttribute("cName", username);
            model.addAttribute("cPass", password);

            request.getSession().setAttribute("tLogged", true);
            return "redirect:log";
        } else {
            model.addAttribute("badpass");
            return "redirect:";
        }
    }

    @GetMapping(value="{id}")
    public String eventPage(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String eventID) {

        boolean logged = checkLog(request, response).isLogged();

        model.addAttribute("logged", logged);

        if (!logged) handleLogoff(request, response);

        ErrorPackage handler = getEvent(model, parseSingleInt(eventID));

        if (handler.hasError()) {
            model.addAttribute("missing", true);
            return "event/static-event";
        }

        model.addAttribute("missing", false);

        BaseEvent targetEvent = (BaseEvent)handler.getAux("event");

        model.addAttribute("targetevent", targetEvent);
        model.addAttribute("participants", targetEvent.getParticipants());

        return "event/static-event";
    }

    @GetMapping(value="{id}/join")
    public String addUser(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String eventID) {

        HttpSession session = request.getSession();

        boolean logged = checkLog(request, response).isLogged();

        model.addAttribute("logged", logged);

        if (logged) {
            RegUser u = findUserByUID((Integer)session.getAttribute("UserID"));

        } else handleLogoff(request, response);

        ErrorPackage handler = getEvent(model, parseSingleInt(eventID));

        if (handler.hasError()) {
            model.addAttribute("missing", true);
            return "event/static-event";
        }

        model.addAttribute("missing", false);

        BaseEvent targetEvent = (BaseEvent)handler.getAux("event");

        return "event/temp-user-login";
    }

    @PostMapping(value="{id}/join")
    public String addUser(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String eventID,
                          @RequestParam("username") String username, @RequestParam("password") String password,
                          @RequestParam("confirm") String confirm) {

        boolean logged = checkLog(request, response).isLogged();

        if (!logged) handleLogoff(request, response);

        ErrorPackage handler = getEvent(model, parseSingleInt(eventID));

        model.addAttribute("missing", handler.hasError());

        if (handler.hasError()) return "event/static-event";

        BaseEvent targetEvent = (BaseEvent)handler.getAux("event");

        Participant p = null;

        if (logged) {
            // Handle registered user join
            int uID = (int)request.getSession().getAttribute("userId");

            for (RegUser ru : regUserDao.findAll()) if (ru.getID() == uID) p = ru;

            regUserDao.save((RegUser)p);
        } else {
            // Handle Temp User join
            if (password.equals(confirm) || (password.isEmpty() && confirm.isEmpty())) {
                p = new TempUser(username, password, targetEvent);
                tempUserDao.save((TempUser)p);
            } else {
                model.addAttribute("passmismatch", true);
                return "redirect:/event/" + eventID + "/join";
            }
        }

        if (targetEvent.isStatic()) {
            ((StaticEvent) targetEvent).addParticipant(p);
            staticEventDao.save((StaticEvent) targetEvent);
        } else {
            // Implement planning event add user. Likely requires outside methods/encapsulation
        }

        participantDao.save(p);
        baseEventDao.save(targetEvent);

        return "redirect:/event/" + eventID;
    }

    // Check temporary login status
    public static boolean checkTLog(HttpSession session) {
        Enumeration<String> sNE = session.getAttributeNames();
        while (sNE.hasMoreElements()) if (sNE.nextElement().equals("tLogged")) return true;
        return false;
    }

    // Feed in parseNextInt(idString)
    private ErrorPackage getEvent(Model model, ErrorPackage input) {
        if (input.hasError()) {
            model.addAttribute("missing", true);
            return input;
        }

        int eID = (int)input.getAux("intOut");

        return getEvent(model, eID);
    }

    // Feed in event ID
    private ErrorPackage getEvent(Model model, int eID) {
        if (!baseEventDao.findById(eID).isPresent()) {
            model.addAttribute("missing", true);
            return yesError("ID corresponds to no existing event");
        }

        model.addAttribute("missing", false);
        ErrorPackage out = noError();
        out.addAux("event", baseEventDao.findById(eID).get());
        out.addAux("eID", eID);
        return out;
    }
}
