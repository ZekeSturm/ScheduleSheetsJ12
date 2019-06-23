package org.CyfrSheets.ScheduleSheets.controllers;

import org.CyfrSheets.ScheduleSheets.models.data.BaseEventDao;
import org.CyfrSheets.ScheduleSheets.models.data.ParticipantDao;
import org.CyfrSheets.ScheduleSheets.models.data.RegUserDao;
import org.CyfrSheets.ScheduleSheets.models.data.StaticEventDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    RegUserDao regUserDao;

    @GetMapping(value = "")
    public String eventHub(Model model) {

        return "event/index";
    }
}
