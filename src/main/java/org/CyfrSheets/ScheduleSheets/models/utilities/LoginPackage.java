package org.CyfrSheets.ScheduleSheets.models.utilities;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginPackage {

    private final boolean logged;

    private final byte[] key;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    // TODO - Add other constructors here

    // Comprehensive constructor
    public LoginPackage(boolean logged, byte[] key, HttpServletRequest request, HttpServletResponse response) {
        this.logged = logged;
        this.key = key;
        this.request = request;
        this.response = response;
    }

    // Empty constructor - simplest delivery of "not logged in"
    public LoginPackage() {
        logged = false;
        key = null;
        request = null;
        response = null;
    }

    public boolean isLogged() { return logged; }

    public void passCookie(Cookie cookie) { response.addCookie(cookie); }


}
