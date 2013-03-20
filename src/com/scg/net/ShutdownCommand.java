package com.scg.net;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 12:19 PM
 *
 * This Command will cause the CommandProcessor to shutdown the server.
 */
public class ShutdownCommand extends Command<Object> {
    private static final long serialVersionUID = -469522035402760610L;

    public ShutdownCommand() {
        super(null);
    }

    /**
     * The method called by the receiver. This method must be implemented by subclasses to send a reference to
     * themselves to the receiver by calling receiver.execute(this).
     */
    @Override
    public void execute() {
       getReceiver().execute(this);
    }
}
