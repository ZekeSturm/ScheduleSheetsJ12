package org.CyfrSheets.ScheduleSheets.models.events;

import org.CyfrSheets.ScheduleSheets.models.users.Participant;
import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.CyfrSheets.ScheduleSheets.models.users.TempUser;
import org.CyfrSheets.ScheduleSheets.models.utilities.EventTime;

import javax.persistence.Entity;

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

    // TempUser constructor
    private StaticEvent(String cName, String cPass) {
        hasUserCreator = false;
        TempUser t = new TempUser(cName, cPass, this);
        this.creatorId = t.getID();
        addParticipant(t);
        keyStore(createKey(t));
    }

    public StaticEvent() { }

    // Constructor compressor - Use userArgs as follows:
    // For RegUser, put <"user",yourRegUser> (String/RegUser)
    // For TempUser, put <"cName",tempUserName> and <"cPass",tempUserPass>
    // (both String/String)
    public static StaticEvent seInit(String eventName, String eventDesc, HashMap<String, Object> userArgs, EventTime time) {
        StaticEvent out;
        if (userArgs.containsKey("user")) {
            RegUser u = (RegUser)userArgs.get("user");
            out = new StaticEvent(u);
        } else {
            String cName = (String)userArgs.get("cName");
            String cPass = (String)userArgs.get("cPass");
            out = new StaticEvent(cName, cPass);
        }

        out.eventName = eventName;
        out.eventDesc = eventDesc;
        out.time = time;

        if (!time.hasEnd()) out.type = SOS;
        else {
            if (time.multiDay()) out.type = MDS;
            else out.type = SDS;
        }

        return out;
    }

    public void addParticipant(Participant p) {
        participants.add(p);
        if (!p.registered()) tempUsers.add((TempUser)p);
    }

}
