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

    // Empty constructor - simplest delivery of "not logged in" - NOTE: When using this, remember to manually kill
    // any existing cookies
    public LoginPackage() {
        logged = false;
        key = null;
        request = null;
        response = null;
    }

    // "Constructor" initializes + cookie - Unsure if necessary for now
    public LoginPackage plateCookie(boolean logged, byte[] key, HttpServletRequest request, HttpServletResponse response, Cookie cookie) {
        LoginPackage out = new LoginPackage(logged, key, request, response);
        out.passCookie(cookie);
        return out;
    }

    public boolean isLogged() { return logged; }

    public boolean badPackage() { return (logged == false && key == null && request == null && response == null); }

    // May be unnecessary
    public void passCookie(Cookie cookie) { response.addCookie(cookie); }


}
