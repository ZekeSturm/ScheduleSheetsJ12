package org.CyfrSheets.ScheduleSheets.models.events;

import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.CyfrSheets.ScheduleSheets.models.users.TempUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.EventTime;

import javax.persistence.Entity;

import java.util.Calendar;
import java.util.HashMap;

import static org.CyfrSheets.ScheduleSheets.models.utilities.EventType.*;

@Entity
public class StaticEvent extends BaseEvent {

    // User constructor
    private StaticEvent(RegUser u) {
        hasUserCreator = true;
        this.creatorId = u.getID();
        addParticipant(u);
        keyStore(createKey(u));
    }

    public StaticEvent() { hasUserCreator = false; }

    // Constructor compressor - Use userArgs as follows:
    // For RegUser, put <"user",yourRegUser> (String/RegUser)
    // For TempUser, put <"cName",tempUserName> and <"cPass",tempUserPass>
    // (both String/String)
    public static StaticEvent seInit(String eventName, String eventDesc, HashMap<String, Object> userArgs, Calendar startTime, Calendar endTime, boolean hasEnd) {
        StaticEvent out;
        boolean regUser = false;
        if (userArgs.containsKey("user") && userArgs.get("user") != null) {
            RegUser u = (RegUser)userArgs.get("user");
            out = new StaticEvent(u);
            regUser = true;
        } else out = new StaticEvent();

        out.eventName = eventName;
        out.eventDesc = eventDesc;

        if (hasEnd) {
            out.addTime(new EventTime(startTime, endTime));
        } else {
            out.addTime(new EventTime(startTime));
        }

        if (!hasEnd) out.type = SOS;
        else {
            if (out.time.multiDay()) out.type = MDS;
            else out.type = SDS;
        }

        return out;
    }

    // Use this for static events with end times
    public static StaticEvent seInit(String eventName, String eventDesc,
                                     HashMap<String, Object> userArgs, Calendar startTime, Calendar endTime)
    { return seInit(eventName, eventDesc, userArgs, startTime, endTime, true); }

    // Use this for static events that are start time only
    public static StaticEvent seInit(String eventName, String eventDesc,
                                     HashMap<String, Object> userArgs, Calendar startTime)
    { return seInit(eventName, eventDesc, userArgs, startTime, null, false); }

    public boolean isStatic() { return true; }

    public void addParticipant(Participant p) {
        participantsInit();
        tempUsersInit();
        participants.add(p);
        if (!p.registered()) addTempUser((TempUser)p);
    }

    public void tempInit(TempUser t) {
        this.creatorId = t.getID();
        addParticipant(t);
        tempUsers.add(t);
        keyStore(createKey(t));
    }
}
