package org.CyfrSheets.ScheduleSheets.models.events;

import org.CyfrSheets.ScheduleSheets.models.users.*;
import org.CyfrSheets.ScheduleSheets.models.utilities.*;

import javax.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    protected String eventName;
    protected String eventDesc;

    // Creator Verification Information
    protected int creatorId;
    protected boolean hasUserCreator;

    protected byte[] creatorKey;

    // Obfuscate creator salt if TempUser
    @ElementCollection
    private List<GrainOfSalt> possibleCreatorSalts = new ArrayList<>();

    @Embedded
    protected EventTime time;

    protected EventType type;

    // All participants
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    protected List<Participant> participants;

    // TempUsers
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "baseevent_id")
    protected List<TempUser> tempUsers;

    // Junk/Error Detection byte for troubleshooting
    protected byte[] badByte = {-1, -1, -1};

    // Possible location field goes here



    // Methods
    public BaseEvent() { }

    // Authorize creator info
    public boolean creatorAuth(Participant p) {
        String pass = eventName + p.getUsername() + creatorId;
        if (p.registered()) {
            RegUser u = (RegUser)p;
            ErrorPackage shakerEP = u.giveTheShaker();
            if (shakerEP.hasError()) return false; // If no salts, they're not it
            ArrayList<byte[]> shaker = (ArrayList<byte[]>)shakerEP.getAux("shaker");
            for (byte[] b : shaker) {
                ErrorPackage cast = makeKey(b, pass);
                if (cast.hasError()) continue;
                if (isKey((byte[])cast.getAux("key"))) return true;
            }
            return false;
        } else {
            for (GrainOfSalt g : possibleCreatorSalts) {
                byte[] b = g.getSalt();
                ErrorPackage cast = makeKey(b, pass);
                if (cast.hasError()) continue;
                if (isKey((byte[])cast.getAux("key"))) return true;
            }
            return false;
        }
    }

    protected boolean keyStore(ErrorPackage e) {
        if (e.hasError()) return false; // Find some way to measure this failure in constructor
        creatorKey = (byte[])e.getAux("key");
        return true;
    }

    // Create creatorKey
    protected ErrorPackage createKey(Participant p) {
        ErrorPackage salty = getSalt();
        if (salty.hasError()) return salty; // Handle errors
        byte[] salt = (byte[]) salty.getAux("salt");

        String pass = eventName + p.getUsername() + creatorId;
        if (p.registered()) {
            RegUser u = (RegUser)p;
            u.passTheSalt(salt);
        } else throwTheShaker(salt);

        return makeKey(salt, pass);
    }

    // Make keys (for storage or comparison)
    private ErrorPackage makeKey(byte[] salt, String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            ErrorPackage out = noError();
            out.addAux("key", md.digest(pass.getBytes()));
            return out;
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - makeKey");
        }
    }

    // Hide creator salt (for tempUsers)
    protected ErrorPackage throwTheShaker(byte[] salt) {
        try {
            Random r = new SecureRandom();
            SecureRandom sR = SecureRandom.getInstance("SHA1PRNG");
            int seed = r.nextInt(100);
            for (int i = 0; i < 100; i++) {
                if (seed == i) possibleCreatorSalts.add(new GrainOfSalt(salt));
                else {
                    byte[] nextByte = new byte[32];
                    sR.nextBytes(nextByte);
                    possibleCreatorSalts.add(new GrainOfSalt(nextByte));
                }
            }
            return noError(true);
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - throwTheShaker");
        }
    }

    // Get salt. Pass to user if necessary
    protected ErrorPackage getSalt() {
        ErrorPackage out;
        try {
            SecureRandom sR = SecureRandom.getInstance("SHA1PRNG");
            byte[] saltByte = new byte[32];
            sR.nextBytes(saltByte);
            out = noError();
            out.addAux("salt", saltByte);
            return out;
        } catch (NoSuchAlgorithmException e) {
            out = yesError("No Such Algorithm Ex - getSalt() [Event ver.]");
            return out;
        }
    }

    protected boolean isKey(byte[] hash) {
        if (creatorKey.length != hash.length) { return false; }
        for (int i = 0; i < hash.length; i++) {
            if (creatorKey[i] != hash[i]) return false;
        }
        return true;
    }

    public int getId() { return id; }
    public String getEventName() { return eventName; }
    public String getEventDesc() { return eventDesc; }
    public EventType getType() { return type; }
    public EventTime getTime() { return time; }
    public List<Participant> getParticipants() { return participants; }

    public ErrorPackage getCreator() {
        if (hasUserCreator) {
            ErrorPackage out = yesError("Has User Creator");
            out.addAux("userCreator", true);
            return out;
        } else {
            for (TempUser t : tempUsers)
                if (t.getID() == creatorId) {
                    ErrorPackage out = noError();
                    out.addAux("creator", t);
                    return out;
                }
            return yesError("No creator found");
        }
    }

    protected void participantsInit() { if (participants == null) participants = new ArrayList<>(); }
    protected void tempUsersInit() { if (tempUsers == null) tempUsers = new ArrayList<>(); }

    protected void addTempUser(TempUser t) {
        tempUsers.add(t);
        t.setParent(this);
    }

    protected void removeTempUser(TempUser t) {
        if (t != null) t.setParent(null);
        if (tempUsers.contains(t)) tempUsers.remove(t);
    }

    protected void addTime(EventTime t) {
        this.time = t;
    }

    protected void deleteTime() {
        this.time = null;
    }
}
