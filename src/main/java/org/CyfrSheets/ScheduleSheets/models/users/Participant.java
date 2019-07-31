package org.CyfrSheets.ScheduleSheets.models.users;

import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    /**
     * was needed for deprecated salt uniqueness
     *
    @Autowired
    private ParticipantDao participantDao;
     */

    @NotNull
    @Size(min = 3, max = 20)
    private String username;

    @ManyToMany(mappedBy = "participants", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<BaseEvent> events;

    private byte[] secPass;
    private byte[] salt;

    // salt init check
    boolean saltNull = true;

    // Methods
    protected Participant() { }

    // Registered user?
    abstract public boolean registered();

    // Check ID without returning ID.
    public boolean checkID(Participant p) { return this.id == p.id; }

    public String getUsername() { return username; }
    public int getID() { return id; }; // TODO - Make protected if it won't break things
    public int getUID() { return -2147483648; } // Futz the check - must be here for use from Participant where necessary

    // Comparator abstract: Body to be split between TempUser and RegUser below
    abstract public boolean equals(Participant p);

    /**
    public boolean equals(Participant p) {
        if (p.isUser != this.isUser) return false;
        if (!p.username.equals(this.username)) return false;
        if (isUser) {
        } else {
            // If hashes are present, check them
            if (secPass != null && salt != null) {
                if (p.secPass == null || p.salt == null) return false;
                if (!isPass(p.secPass)) return false;
            } else if (p.secPass != null || p.salt != null) return false;
        }
        return true;
    } */

    // Encrypt given password to check if it hashes into the object hash
    public ErrorPackage checkPassword(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return noError(isPass(md.digest(pass.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - checkPassword");
        }
    }

    // TODO - Move method below to RegUser - unnecessary for TempUser
    // Attempt to change password. Returns whether or not it succeeded, and what caused it to fail if so
    public ErrorPackage attemptChangePass(String pass, String newPass) {
        // Check current password for match first
        ErrorPackage eP = checkPassword(pass);
        // Check for errors and successful pass match
        if (!eP.hasError() && eP.getAncil()) return changePassword(newPass);
        if (!eP.hasError() && !eP.getAncil()) eP.setMessage("Passwords Do Not Match!");
        return eP;
    }

    // TODO - Move method below to RegUser - unnecessary for TempUser
    // Private change password interface - Separate more as a failsafe than anything, in case anything must be changed
    private ErrorPackage changePassword(String pass) {
        return securePassword(pass);
    }

    protected void setUsername(String username) { this.username = username; }

    // Setting password first time, changing it in future. Makes changePassword redundant for now
    protected ErrorPackage securePassword(String pass) {
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
    protected boolean isPass(byte[] hash) {
        if (secPass.length != hash.length) { return false; }
        for (int i = 0; i < hash.length; i++) {
            if (secPass[i] != hash[i]) {
                return false;
            }
        }
        return true;
    }



    // Get a new salt and save it. Called every time a password is made or changed
    protected ErrorPackage shakeSalt() {
        if (saltNull) {
            salt = new byte[32];
            saltNull = false;
        }
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
