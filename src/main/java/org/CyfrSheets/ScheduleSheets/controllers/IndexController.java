package org.CyfrSheets.ScheduleSheets.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.CyfrSheets.ScheduleSheets.models.utilities.LoginUtil.checkLog;

@Controller
public class IndexController {

    @GetMapping(value = {"", "/"})
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        return "forward:/event";
    }

    @GetMapping(value = "login")
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {
        return "forward:/user/login";
    }

    @PostMapping(value = "login")
    public String login(Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username, @RequestParam("password") String password) {
        return "forward:/user/login";
    }

    @GetMapping(value = "home")
    public String userHome(Model model, HttpServletRequest request, HttpServletResponse response) {

        boolean logged = checkLog(request, response).isLogged();

        if (logged) {
            // fetch user ID from session
            int uID = (int) request.getSession().getAttribute("userId");
            model.addAttribute("title", "Your profile");
            return "forward:/user/profile/" + uID;
        } else {
            return "forward:/event";
        }
    }
}
