package org.CyfrSheets.ScheduleSheets.models.utilities;

import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class GrainOfSalt {

    private byte[] salt;

    public GrainOfSalt(byte[] salt) { this.salt = salt; }

    public GrainOfSalt() { }

    public byte[] getSalt() { return salt; }
}
