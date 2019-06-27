package org.CyfrSheets.ScheduleSheets.models.utilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginPackage {

    private boolean logged;

    private boolean registered;

    private byte[] key;

    HttpServletRequest request;

    HttpServletResponse response;

    // TODO - Add other constructors here
    public LoginPackage(boolean logged, HttpServletRequest request, HttpServletResponse response) { }

    // Empty constructor - simplest delivery of "not logged in"
    public LoginPackage() { logged = false; }

    public boolean isLogged() { return logged; }
    public boolean isRegistered() { return registered; }


}
