package org.CyfrSheets.ScheduleSheets.models.users;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidPasswordException;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

@Entity
public class RegUser extends Participant {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uID;

    @NotNull
    private String emailAddr;

    // Session key
    private byte[] seshKey = null;

    // List of salts for keys for created events & array initializer bool
    private ArrayList<byte[]> cSaltList;
    private boolean cSLYN = false;

    // Methods
    public RegUser(String name, String pass, String emailAddr) throws InvalidPasswordException {
        if (pass.isEmpty() || pass.isBlank()) throw new InvalidPasswordException("RegUser Constructor Missing Password");
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

    public int getUID() { return uID; }

    public boolean checkUID(Participant p) {
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

    public ErrorPackage keyGen(String pass) {
        ErrorPackage handler = checkPassword(pass);
        if (handler.hasError()) return handler;
        if (Boolean.valueOf((String)handler.getAux("ancil"))) {
            // Should trigger on all non-registration/login calls
            if (seshKey != null) {
                handler = noError();
                handler.addAux("sKey", seshKey);
                return handler;
            }
            // Should only be necessary for registration
            String keyStr = pass + getUsername() + getID() + getUID();
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                seshKey = md.digest(keyStr.getBytes());
                handler = noError();
                handler.addAux("sKey", seshKey);
            } catch (NoSuchAlgorithmException e) {
                handler = yesError("No such algorithm exception - keyGen");
            }
            return handler;
        } else {
            return yesError("Password Mismatch", false);
        }
    }

    // This pair of methods is redundant, fix later
    public boolean checkKey(byte[] key) { return isKey(key); }

    private boolean isKey(byte[] hash) {
        for (int i = 0; i < hash.length ; i++) if (hash[i] != seshKey[i]) return false;
        return true;
    }

    private void cSaltListInit() {
        if (cSLYN) return;
        cSLYN = true;
        cSaltList = new ArrayList<>();
    }
}
