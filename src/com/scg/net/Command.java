package com.scg.net;

import com.scg.net.server.CommandProcessor;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 11:35 AM
 *
 * The superclass of all Command objects, implements the command role in the Command design pattern.
 */
public abstract class Command<T> implements Serializable {
    T target;
    private transient CommandProcessor receiver;

    /**
     * Construct an AbstractCommand with a receiver; called from subclasses.
     * @param target - the target
     */
    public Command(final T target) {
        this.target = target;
    }

    /**
     * The method called by the receiver. This method must be implemented by subclasses to send a reference to
     * themselves to the receiver by calling receiver.execute(this).
     */
    public abstract void execute();

    /**
     * Gets the CommandProcessor receiver for this Command.
     * @return - the receiver for this Command.
     */
    public final CommandProcessor getReceiver() {
        return receiver;
    }

    /**
     * Set the CommandProcessor that will execute this Command.
     * @param receiver - the receiver for this Command.
     */
    public final void setReceiver(final CommandProcessor receiver) {
        this.receiver = receiver;
    }

    /**
     * public final T getTarget()
     * @return - the target
     */
    public final T getTarget() {
        return target;
    }
}
