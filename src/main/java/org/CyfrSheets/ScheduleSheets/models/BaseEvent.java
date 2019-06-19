package org.CyfrSheets.ScheduleSheets.models;

import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.noError;
import static org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage.yesError;

@Entity
abstract class BaseEvent {

    @Id
    @GeneratedValue
    private int id;

    private String eventName;
    private String eventDesc;

    // Creator Verification Information
    private int creatorId;
    private boolean hasUserCreator;

    private byte[] creatorKey;

    // Obfuscate creator salt
    private ArrayList<byte[]> possibleCreatorSalts = new ArrayList<>();

    @OneToOne
    private EventTime time;

    private EventType type;

    // All participants
    @ManyToMany
    private List<Participant> participants;

    // Junk/Error Detection byte for troubleshooting
    private byte[] badByte = {-1, -1, -1};

    // Possible location field goes here

    public BaseEvent() { }

    // Add any participant - should take in user data and convert to user or tempuser for submission
    abstract public void addParticipant();

    // Authorize creator info
    public boolean creatorAuth(Participant p) {
        // TODO - Implement

        // Error stopper. Remove once implemented
        return true;
    }

    // Create creator key
    private void createKey(Participant p) {
        // TODO - Implement
    }

    // Hide creator salt
    protected ErrorPackage throwTheShaker(byte[] salt) {
        try {
            Random r = new SecureRandom();
            SecureRandom sR = SecureRandom.getInstance("SHA1PRNG");
            int seed = r.nextInt(100);
            for (int i = 0; i < 100; i++) {
                if (seed == i) possibleCreatorSalts.add(salt);
                else {
                    byte[] nextByte = new byte[32];
                    sR.nextBytes(nextByte);
                    possibleCreatorSalts.add(nextByte);
                }
            }
            return noError(true);
        } catch (NoSuchAlgorithmException e) {
            return yesError("No Such Algorithm Ex - throwTheShaker");
        }
    }

    public int getId() { return id; }
    public String getEventName() { return eventName; }
    public String getEventDesc() { return eventDesc; }
    public EventType getType() { return type; }
    public EventTime getTime() { return time; }
    public List<Participant> getParticipants() { return participants; }

}
