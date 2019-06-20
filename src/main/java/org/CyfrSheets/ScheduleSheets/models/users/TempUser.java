package org.CyfrSheets.ScheduleSheets.models.users;

import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;
import org.CyfrSheets.ScheduleSheets.models.utilities.ErrorPackage;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class TempUser extends Participant {

    @ManyToOne
    private final BaseEvent parent;

    // Methods
    protected TempUser(String name, String pass, BaseEvent parent) {
        ErrorPackage ep;
        if (!pass.equals("")) ep = securePassword(pass);
        setName(name);
        this.parent = parent;
    }

    // No one gets to use this ever.
    private TempUser() { parent = null; }

    // Registered user? (No)
    public boolean registered() { return false; }

    public boolean equals(Participant p) {
        if (p.registered()) return false;
        if (!checkID(p.getID())) return false;
        if (!this.getName().equals(p.getName())) return false;
        return true;
    }

    public BaseEvent getParent() { return parent; }
}
