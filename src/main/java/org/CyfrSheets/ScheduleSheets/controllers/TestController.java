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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
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

    @GetMapping(value = "session")
    public String sessionTest (Model model, HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession();

        HashMap<String, String> sessionObjs = new HashMap<>();

        Enumeration<String> sO = session.getAttributeNames();

        Cookie[] cookies = request.getCookies();
        if (cookies.length != 0) for (Cookie c : cookies) if (c.getName().equals("Login") && c.getValue().equals("true")) {
            while (sO.hasMoreElements()) {
                String next = sO.nextElement();
                sessionObjs.put(next, (String) session.getAttribute(next));
            }
            model.addAttribute("logged", true);
        } else model.addAttribute("logged", false);

        model.addAttribute("list", sessionObjs);
        ArrayList<String> keyset = new ArrayList<>(sessionObjs.keySet());
        model.addAttribute("keyset", keyset);

        return "test/session";
    }

    @PostMapping(value = "session")
    public String sessionTest (Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam("key") String key, @RequestParam("value") String value, @RequestParam("logInOut") String inOutString) {

        HttpSession session = request.getSession();

        boolean login = parseBool(inOutString);
        Cookie[] cookies = request.getCookies();
        boolean already = false;
        if (cookies.length != 0) for (Cookie c : cookies) if (c.getName().equals("Login") && c.getValue().equals("true")) already = true;

        if (login) {
            session.setAttribute(key, value);
            if (!already) {
                Cookie logged = new Cookie("Login", "true");
                logged.setMaxAge(600);
                response.addCookie(logged);
            } else {
                for (Cookie c : cookies)
                    if (c.getName().equals("Login") && c.getValue().equals("true")) {
                        c.setMaxAge(c.getMaxAge() + 600);
                        break;
                    }
            }
        } else {
            session.invalidate();
            if (already) for (Cookie c : cookies) if (c.getName().equals("Login") && c.getValue().equals("true")) {
                c.setMaxAge(0);
                response.addCookie(c);
                break;
            }
        }

        return "redirect:/test/session";
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
