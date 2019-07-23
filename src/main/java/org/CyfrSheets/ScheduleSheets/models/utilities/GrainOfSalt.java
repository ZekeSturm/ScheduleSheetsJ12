package org.CyfrSheets.ScheduleSheets.models.utilities;

import javax.persistence.Embeddable;

// TODO - Class useless. Delete after next commit if events can still be created properly with temp users after testing

@Embeddable
public class GrainOfSalt {

    private byte[] salt;

    public GrainOfSalt(byte[] salt) { this.salt = salt; }

    public GrainOfSalt() { }

    public byte[] getSalt() { return salt; }
}
