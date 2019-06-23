package org.CyfrSheets.ScheduleSheets.models.users;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidPasswordException;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

@Entity
public class RegUser extends Participant {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private int uID;

    @NotNull
    private String emailAddr;

    // List of salts for keys for created events & array initializer bool
    private ArrayList<byte[]> cSaltList;
    private boolean cSLYN = false;

    // Methods
    protected RegUser(String name, String pass, String emailAddr) throws InvalidPasswordException {
        ErrorPackage ep = new ErrorPackage();
        if (pass.isEmpty() || pass.isBlank()) ep = yesError("Empty password in RegUser constructor");
        if (ep.hasError()) throw new InvalidPasswordException("RegUser Constructor Missing Password");
        setUsername(name);
        this.emailAddr = emailAddr;
        securePassword(pass);
    }

    // Registered user? (Yes)
    public boolean registered() { return true; }

    public boolean equals(Participant p) {
        if (!p.registered()) return false;
        RegUser u = (RegUser)p;
        if (!checkID(u)) return false;
        if (!emailAddr.equals(u.getEmail())) return false;
        return true;
    }

    public String getEmail() { return emailAddr; }

    public int getID() { return uID; }

    public boolean checkID(Participant p) {
        if (!p.registered()) return false;
        return p.getID() == uID;
    }

    public void passTheSalt(byte[] salt) {
        cSaltListInit();
        cSaltList.add(salt);
    }

    public ErrorPackage giveTheShaker() {
        if (cSLYN) {
            ErrorPackage out = noError();
            out.addAux("shaker", cSaltList);
            return out;
        } else {
            return yesError("No creator salts found");
        }
    }

    private void cSaltListInit() {
        if (cSLYN) return;
        cSLYN = true;
        cSaltList = new ArrayList<>();
    }
}
