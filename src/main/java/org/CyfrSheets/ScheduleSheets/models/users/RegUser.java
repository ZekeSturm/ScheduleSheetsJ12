package org.CyfrSheets.ScheduleSheets.models.users;

import org.CyfrSheets.ScheduleSheets.models.exceptions.InvalidPasswordException;
import org.CyfrSheets.ScheduleSheets.models.utilities.ClassCase;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ClassChecker.checkClass;

@Entity
@SequenceGenerator(name = "regseq", initialValue = 1, allocationSize = 2147483645)
public class RegUser extends Participant {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false, nullable = false)
    private int uID;

    @NotNull
    private String emailAddr;

    // Session key
    private byte[] seshKey = new byte[32];

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

    public RegUser() { }

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

    // Generate new random session key
    public ErrorPackage keyGen() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(seshKey);
            ErrorPackage handler = noError();
            handler.addAux("key", seshKey);
            return handler;
        } catch (NoSuchAlgorithmException e) {
            return yesError(e.getMessage() + " - in keyGen(no args)");
        }
    }

    public boolean checkKey(byte[] hash) {
        for (int i = 0; i < hash.length ; i++) if (hash[i] != seshKey[i]) return false;
        return true;
    }

    private void cSaltListInit() {
        if (cSLYN) return;
        cSLYN = true;
        cSaltList = new ArrayList<>();
    }
}
