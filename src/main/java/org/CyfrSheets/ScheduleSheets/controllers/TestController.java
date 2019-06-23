package org.CyfrSheets.ScheduleSheets.controllers;


import org.CyfrSheets.ScheduleSheets.models.data.*;
import org.CyfrSheets.ScheduleSheets.models.events.StaticEvent;
import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidDateTimeArrayException;
import org.CyfrSheets.ScheduleSheets.models.users.TempUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Calendar;
import java.util.HashMap;

import static org.CyfrSheets.ScheduleSheets.models.events.StaticEvent.seInit;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ParserUtil.*;

@Controller
@RequestMapping(value = "test")
public class TestController {

    @Autowired
    private BaseEventDao baseEventDao;

    @Autowired
    private ParticipantDao participantDao;

    @Autowired
    private RegUserDao regUserDao;

    @Autowired
    private StaticEventDao staticEventDao;

    @GetMapping(value = "")
    public String index (Model model) {

        model.addAttribute("events", baseEventDao.findAll());
        model.addAttribute("statevents", staticEventDao.findAll());
        model.addAttribute("participants", participantDao.findAll());
        model.addAttribute("regusers", regUserDao.findAll());

        model.addAttribute("title","Test Index");

        return "test/index";
    }

    @GetMapping(value = "event")
    public String addEvent (Model model) {

        model.addAttribute("badpass", false);
        model.addAttribute("baddta", false);
        model.addAttribute("title", "Add Event");

        return "test/event";
    }

    @PostMapping(value = "event")
    public String addEvent
            (Model model, @RequestParam String username, @RequestParam String password, @RequestParam String conpass,
             @RequestParam String eventname,@RequestParam String eventdesc, @RequestParam String starttime,
             @RequestParam String endtime) {


        if (password.equals(conpass)) {

            model.addAttribute("badpass", false);

            HashMap<String, Object> args = new HashMap<>();
            args.put("cName", username);
            args.put("cPass", password);

            Calendar.Builder cb = new Calendar.Builder();
            cb.setCalendarType("gregorian");

            ErrorPackage handler;
            try {
                int[] dtInts = parseDateAndTime(starttime);
                cb.setDate(dtInts[0], dtInts[1], dtInts[2]);
                cb.setTimeOfDay(dtInts[3], dtInts[4], dtInts[5]);

                Calendar sT = cb.build();

                handler = noError();
                handler.addAux("startTime", sT);
            } catch (InvalidDateTimeArrayException e) {
                handler = yesError(e.getMessage());
            }

            ErrorPackage hTwo;
            try {
                int[] etInts = parseDateAndTime(endtime);
                cb.setDate(etInts[0], etInts[1], etInts[2]);
                cb.setTimeOfDay(etInts[3], etInts[4], etInts[5]);
                Calendar eT = cb.build();

                hTwo = noError();
                handler.addAux("endTime", eT);
            } catch (InvalidDateTimeArrayException e) {
                hTwo = yesError(e.getMessage());
            }

            if (handler.hasError()) {
                model.addAttribute("baddta", true);
                model.addAttribute("title", "Add Event");

                return "test/event";
            }

            StaticEvent out;

            if (!hTwo.hasError()) out = seInit(eventname, eventdesc, args, (Calendar)handler.getAux("startTime"),
                    (Calendar)handler.getAux("endTime"));
            else out = seInit(eventname, eventdesc, args, (Calendar)handler.getAux("startTime"));

            TempUser tu = new TempUser(username, password, out);

            out.tempInit(tu);

            baseEventDao.save(out);
            staticEventDao.save(out);

            participantDao.save(tu);


            model.addAttribute("events", baseEventDao.findAll());
            model.addAttribute("statevents", staticEventDao.findAll());
            model.addAttribute("participants", participantDao.findAll());
            model.addAttribute("regusers", regUserDao.findAll());

            return "redirect:";

        } else {
            model.addAttribute("badpass", true);
            model.addAttribute("title", "Add Event");

            return "test/event";
        }
    }
}
