package com.scg.domain;

import com.scg.util.Name;

import javax.swing.event.EventListenerList;
import java.beans.*;
import java.util.EventListenerProxy;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 1/31/13
 * Time: 8:00 AM
 */
public class StaffConsultant extends Consultant {
    static final long serialVersionUID = -5366268816864513537L;

    public static final String PAY_RATE_PROPERTY_NAME          = "payRate";
    public static final String SICK_LEAVE_HOURS_PROPERTY_NAME  = "sickLeaveHours";
    public static final String VACATION_HOURS_PROPERTY_NAME    = "vacationHours";

    /** Factor used in calculating hashCode. */
    private static final int HASH_FACTOR = 37;

    private int payRate;
    private int sickLeaveHours;
    private int vacationHours;

    private final VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public StaffConsultant(final Name name,
                           final int rate,
                           final int sickLeave,
                           final int vacation) {
        super(name);
        this.payRate = rate;
        this.sickLeaveHours = sickLeave;
        this.vacationHours = vacation;
    }


    public int getPayRate() {
        return payRate;
    }

    public void setPayRate(final int payRate) throws PropertyVetoException {
        int oldValue = getPayRate();
        vetoSupport.fireVetoableChange(PAY_RATE_PROPERTY_NAME, oldValue, payRate);
        this.payRate = payRate;
        changeSupport.firePropertyChange(PAY_RATE_PROPERTY_NAME, oldValue, payRate);
    }

    public int getSickLeaveHours() {
        return sickLeaveHours;
    }

    public void setSickLeaveHours(final int sickLeaveHours) {
        final int oldHours = getSickLeaveHours();
        this.sickLeaveHours = sickLeaveHours;
        changeSupport.firePropertyChange(SICK_LEAVE_HOURS_PROPERTY_NAME, oldHours, sickLeaveHours);
    }

    public int getVacationHours() {
        return vacationHours;
    }

    public void setVacationHours(final int vacationHours) throws PropertyVetoException {
        final int oldHours = getVacationHours();
        this.vacationHours = vacationHours;
        changeSupport.firePropertyChange(VACATION_HOURS_PROPERTY_NAME, oldHours, vacationHours);
    }

    /**
     * Adds a general property change listener.
     * @param l - the listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove a general property change listener.
     * @param l - the listener
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /**
     * Adds a payRate property change listener.
     * @param l - the listener
     */
    public void addPayRateListener(final PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(PAY_RATE_PROPERTY_NAME, l);
    }

    /**
     * Removes a payRate property change listener.
     * @param l - the listener
     */
    public void removePayRateListener(final PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /**
     * Adds a vetoable change listener.
     * @param l - the listener
     */
    public void addVetoableChangeListener(final VetoableChangeListener l) {
        vetoSupport.addVetoableChangeListener(l);
    }

    /**
     * Removes a vetoable change listener.
     * @param l - the listener
     */
    public void removeVetoableChangeListener(final VetoableChangeListener l) {
        vetoSupport.removeVetoableChangeListener(l);
    }

    public synchronized void addSickLeaveHoursListener(final PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(SICK_LEAVE_HOURS_PROPERTY_NAME, listener);
    }

    public synchronized void addVacationHoursListener(final PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(VACATION_HOURS_PROPERTY_NAME, listener);
    }

    public synchronized void removeSickLeaveHoursListener(final PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void removeVacationHoursListener(final PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Calculate the hash code.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        int hc = StaffConsultant.class.hashCode();
        hc *= HASH_FACTOR + ((getName() == null) ? 0 : getName().hashCode());
        hc += hc * HASH_FACTOR + payRate;
        hc += hc * HASH_FACTOR + sickLeaveHours;
        hc += hc * HASH_FACTOR + vacationHours;

        return hc;
    }

    /**
     * Compare names for equivalence.
     *
     * @param other the name to compare to
     *
     * @return true if all the name elements have the same value
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        final StaffConsultant o = (StaffConsultant)other;

        return getName().equals(o.getName()) &&
                payRate == o.payRate &&
                sickLeaveHours == o.sickLeaveHours &&
                vacationHours == o.vacationHours;
    }
}
