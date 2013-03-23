package com.scg.domain;

import java.util.EventObject;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/2/13
 * Time: 9:18 AM
 *
 * Event used to notify listeners of a Consultant's termination.
 */
public class TerminationEvent extends EventObject {
    static final long serialVersionUID = 2049575119145182018L;

    private final Consultant consultant;
    private boolean voluntary;

    public TerminationEvent(final Object source, final Consultant consultant, final boolean voluntary) {
        super(source);
        this.consultant = consultant;
        this.voluntary = voluntary;
    }

    public Consultant getConsultant() {
        return consultant;
    }

    public boolean isVoluntary() {
        return voluntary;
    }
}
