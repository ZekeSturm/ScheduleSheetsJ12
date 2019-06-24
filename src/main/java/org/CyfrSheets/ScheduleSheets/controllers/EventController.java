package org.CyfrSheets.ScheduleSheets.controllers;

import org.CyfrSheets.ScheduleSheets.models.data.*;
import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;
import org.CyfrSheets.ScheduleSheets.models.events.StaticEvent;
import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;
import org.CyfrSheets.ScheduleSheets.models.users.TempUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.HashMap;

import static org.CyfrSheets.ScheduleSheets.models.events.StaticEvent.seInit;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

@Controller
@RequestMapping(value = "event")
public class EventController {

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
    public String eventHub(Model model) {

        model.addAttribute("title", "Events");

        return "event/index";
    }

    @GetMapping(value = "new/static")
    public String newStaticEvent(Model model) {
        model.addAttribute("logged", false);
        model.addAttribute("badpass", false);
        model.addAttribute("title", "Create your new event!");

        return "event/new-static";
    }


    // Old newStaticEvent method body
    /**
     if (!model.containsAttribute("logged")) {
     model.addAttribute("logged", false);
     }
     if ((boolean)model.getAttribute("logged")) {
     model.addAttribute("title", "Create your new event!");
     return "event/new-static";
     } else {
     return "redirect:static/temp";
     }
     */

    // No use right now. Will fix this later. Need MVP now.
    /**
    @PostMapping(value = "new/static/log")
    public String logStaticEvent(Model model, @RequestParam("cName") String cName,
                                 @RequestParam("cPass") String cPass) {
        model.addAttribute("cName", cName);
        model.addAttribute("cPass", cPass);
        model.addAttribute("title", "Create your new event!");

        return "event/new-static";
    } */

    @PostMapping(value = "new/static")
    public String newStaticEvent(Model model, @RequestParam("eventName") String name,
                                 @RequestParam("eventDesc") String desc, @RequestParam("startDate") String startDate,
                                 @RequestParam("startTime") String startTime, @RequestParam("endDate") String endDate,
                                 @RequestParam("endTime") String endTime, @RequestParam("cName") String cName,
                                 @RequestParam("cPass") String cPass, @RequestParam("confirm") String confirm) {

        boolean passmatch;
        if (cPass.equals(confirm)) {
            passmatch = true;
        } else {
            model.addAttribute("badpass", true);
            model.addAttribute("logged", false);
            model.addAttribute("title", "Create your new event!");
            return "redirect:";
        }

        Calendar.Builder cb = new Calendar.Builder();
        cb.setCalendarType("gregorian");

        ErrorPackage handler;
        try {
            int[] sDA = parseDate(startDate);
            int[] sTA = parseTime(startTime);

            cb.setDate(sDA[0], sDA[1], sDA[2]);
            cb.setTimeOfDay(sTA[0], sTA[1], sTA[2]);

            Calendar sT = cb.build();

            handler = noError();
            handler.addAux("startTime", sT);
        } catch (InvalidDateTimeArrayException e) {
            handler = yesError(e.getMessage());
        }

        ErrorPackage hTwo;
        try {
            int[] eDA = parseDate(endDate);
            int[] eTA = parseDate(endTime);

            cb.setDate(eDA[0], eDA[1], eDA[2]);
            cb.setTimeOfDay(eTA[0], eTA[1], eTA[2]);

            Calendar eT = cb.build();

            hTwo = noError();
            handler.addAux("endTime", eT);
        } catch (InvalidDateTimeArrayException e) {
            hTwo = yesError(e.getMessage());
        }

        if (handler.hasError()) {
            model.addAttribute("baddta", true);
            model.addAttribute("title", "Create your new event!");
            model.addAttribute("logged", true);
            model.addAttribute("cName", cName);
            model.addAttribute("cPass", cPass);

            return "redirect:new/static";
        }

        StaticEvent out;

        HashMap<String, Object> args = new HashMap<>();

        if (!hTwo.hasError()) out = seInit(name, desc, args, (Calendar)handler.getAux("startTime"),
                (Calendar)handler.getAux("endTime"));
        else out = seInit(name, desc, args, (Calendar)handler.getAux("startTime"));

        TempUser tu = new TempUser(cName, cPass, out);

        out.tempInit(tu);

        baseEventDao.save(out);
        staticEventDao.save(out);

        participantDao.save(tu);
        tempUserDao.save(tu);

        return "redirect:/event/" + out.getId();
    }

    @GetMapping(value="new/static/temp")
    public String newStaticTempCreator(Model model) {

        model.addAttribute("creating", true);
        model.addAttribute("title", "Register for your event");

        return "event/temp-user-login";
    }

    @PostMapping(value="new/static/temp")
    public String newTempCreator(Model model, @RequestParam("username") String username,
                                 @RequestParam("password") String password, @RequestParam("confirm") String confirm) {

        if (password.equals(confirm)) {

            model.addAttribute("cName", username);
            model.addAttribute("cPass", password);
            return "redirect:log";
        } else {
            model.addAttribute("badpass");
            return "redirect:temp";
        }
    }

    @GetMapping(value="{id}")
    public String eventPage(Model model, @PathVariable("id") String eventID) {

        ErrorPackage handler = getEvent(model, parseNextInt(eventID));

        if (handler.hasError()) return "event/static-event";

        model.addAttribute("missing", false);

        BaseEvent targetEvent = (BaseEvent)handler.getAux("event");

        model.addAttribute("targetevent", targetEvent);
        model.addAttribute("participants", targetEvent.getParticipants());

        return "event/static-event";
    }

    @GetMapping(value="{id}/join")
    public String addUser(Model model, @PathVariable("id") String eventID) {

        ErrorPackage handler = getEvent(model, parseNextInt(eventID));

        if (handler.hasError()) return "event/static-event";

        model.addAttribute("missing", false);

        BaseEvent targetEvent = (BaseEvent)handler.getAux("event");

        return "event/temp-user-login";
    }

    @PostMapping(value="{id}/join")
    public String addUser(Model model, @PathVariable("id") String eventID, @RequestParam("username") String username,
                          @RequestParam("password") String password, @RequestParam("confirm") String confirm) {

        ErrorPackage handler = getEvent(model, parseNextInt(eventID));

        if (handler.hasError()) return "event/static-event";

        model.addAttribute("missing", false);

        BaseEvent targetEvent = (BaseEvent)handler.getAux("event");

        if (password.equals(confirm) || (password.isEmpty() && confirm.isEmpty())) {
            TempUser tu = new TempUser(username, password, targetEvent);
            participantDao.save(tu);
            tempUserDao.save(tu);
            if (targetEvent.isStatic()) {
                ((StaticEvent)targetEvent).addParticipant(tu);
                staticEventDao.save((StaticEvent)targetEvent);
            } else {
                // Implement planning event add user. Likely requires outside methods/encapsulation
            }
            baseEventDao.save(targetEvent);
        } else {
            model.addAttribute("passmismatch", true);
            return "redirect:/event/" + eventID + "/join";
        }

        return "redirect:/event/" + eventID;
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
