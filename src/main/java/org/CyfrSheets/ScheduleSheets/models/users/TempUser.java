package org.CyfrSheets.ScheduleSheets.models.users;

import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.*;

@Entity
public class TempUser extends Participant {

    @GeneratedValue(strategy = GenerationType.TABLE)
    int tID;

    @ManyToOne
    private BaseEvent parent;

    // Methods
    public TempUser(String name, String pass, BaseEvent parent) {
        ErrorPackage ep;
        if (!pass.equals("")) ep = securePassword(pass);
        setUsername(name);
        this.parent = parent;
    }

    public TempUser() { }

    public int getTID() { return tID; }

    public boolean checkTID(Participant p) {
        if (p.registered()) return false;
        return p.getID() == tID;
    }

    // Registered user? (No)
    public boolean registered() { return false; }

    public boolean equals(Participant p) {
        if (p.registered()) return false;
        if (!checkID(p)) return false; // May make above line redundant - will return false if p.registered()
        if (!this.getUsername().equals(p.getUsername())) return false;
        return true;
    }

    public BaseEvent getParent() { return parent; }

    public void setParent(BaseEvent e) { parent = e; }
}
