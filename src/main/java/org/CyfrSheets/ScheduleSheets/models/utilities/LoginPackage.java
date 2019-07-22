package org.CyfrSheets.ScheduleSheets.models.utilities;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginPackage {

    private final boolean logged;

    private final byte[] key;

    private final HttpSession session;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    // TODO - Add other constructors here

    public LoginPackage(boolean logged, byte[] key, HttpSession session, HttpServletRequest request,
                        HttpServletResponse response) {
        this.logged = logged;
        this.key = key;
        this.session = session;
        this.request = request;
        this.response = response;
    }

    // Comprehensive constructor
    public LoginPackage(boolean logged, byte[] key, HttpServletRequest request, HttpServletResponse response) {
        this(logged, key, request.getSession(), request, response); }


    // Session clear constructor - passes along a session and nothing else
    public LoginPackage(HttpSession session) {
        this(false, null, session, null, null);
    }

    // Empty constructor - simplest delivery of "not logged in" - NOTE: When using this, remember to manually kill
    // any existing cookies
    public LoginPackage() {
        logged = false;
        key = null;
        session = null;
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

    public boolean badPackage() { return (logged == false && key == null && session == null && request == null && response == null); }

    public HttpSession getSession() { return session; }

    public byte[] getKey() { return key; }

    // May be unnecessary
    public void passCookie(Cookie cookie) { response.addCookie(cookie); }


}
