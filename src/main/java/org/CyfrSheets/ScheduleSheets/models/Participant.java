package org.CyfrSheets.ScheduleSheets.models;

import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

@Entity
public class Participant {

    @Id
    @GeneratedValue
    private int id;

    /**
     * was needed for deprecated salt uniqueness
     *
    @Autowired
    private ParticipantDao participantDao;
     */

    @NotNull
    @Size(min = 3, max = 20)
    private String name;

    @ManyToMany(mappedBy = "participants")
    private List<BaseEvent> events;

    private boolean isUser;

    private byte[] secPass = null;
    private byte[] salt = null;

    // Methods
    public Participant() { }

    public boolean registered() { return isUser; }

    // Check ID without returning ID.
    // TODO - Phase out getID for this if possible
    public boolean checkID(int id) { return this.id == id; }

    public String getName() { return name; }
    public int getID() { return id; }

    // Comparator
    public boolean equals(Participant p) {
        if (p.isUser != this.isUser) return false;
        if (!p.name.equals(this.name)) return false;
        if (isUser) {
            // TODO - Check email & hashes w/ User-specific submethod.
        } else {
            // If hashes are present, check them
            if (secPass != null && salt != null) {
                if (p.secPass == null || p.salt == null) return false;
                if (!isHash(p.secPass)) return false;
            } else if (p.secPass != null || p.salt != null) return false;
        }
        return true;
    }

    // Encrypt given password to check if it hashes into the object hash
    public ErrorPackage checkPassword(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return noError(isHash(md.digest(pass.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - checkPassword");
        }
    }

    // Attempt to change password. Returns whether or not it succeeded, and what caused it to fail if so
    public ErrorPackage attemptChangePass(String pass, String newPass) {
        // Check current password for match first
        ErrorPackage eP = checkPassword(pass);
        // Check for errors and successful pass match
        if (!eP.hasError() && eP.getAncil()) return changePassword(newPass);
        if (!eP.hasError() && !eP.getAncil()) eP.setMessage("Passwords Do Not Match!");
        return eP;
    }

    // Private change password interface - Separate more as a failsafe than anything, in case anything must be changed
    private ErrorPackage changePassword(String pass) {
        return securePassword(pass);
    }

    // Setting password first time, changing it in future. Makes changePassword redundant for now
    private ErrorPackage securePassword(String pass) {
        if ( pass == null ) return yesError("No string handed to securePass");
        ErrorPackage saltEP = shakeSalt();
        if (saltEP.hasError()) return saltEP;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            secPass = md.digest(pass.getBytes());
            return noError(true);
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - securePassword (MD.getInstance)");
        }
    }

    // Check password hash vs given hash. Do not allow anything public facing to use this within three degrees of directly
    private boolean isHash(byte[] hash) {
        if (secPass.length != hash.length) { return false; }
        for (int i = 0; i < hash.length; i++) {
            if (secPass[i] != hash[i]) {
                return false;
            }
        }
        return true;
    }

    // Get a new salt and save it. Called every time a password is made or changed
    private ErrorPackage shakeSalt() {
        try {
            SecureRandom sR = SecureRandom.getInstance("SHA1PRNG");
            sR.nextBytes(salt);
            return noError(true);
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - shakeSalt");
        }
    }

    /**
     * Salt duplication protection rendered irrelevant for now
     *
    private boolean usedSalt(byte[] salt) {
        for (Participant p : participantDao.findAll()) {
            boolean same = true;
            for (int i = 0; i < salt.length; i++) {
                if (p.salt[i] != salt[i]) same = false;
            }
            if (same) return true;
        }
        return false;
    }
     */
}
