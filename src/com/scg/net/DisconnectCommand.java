package com.scg.net;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 12:14 PM
 *
 * The command to disconnect, this commandhas no target.
 */
public class DisconnectCommand extends Command<Object> {
    private static final long serialVersionUID = 580539662031697011L;

    /**
     * Construct an DisconnectCommand.
     */
    public DisconnectCommand() {
        super(null);
    }

    /**
     * Execute this Command by calling receiver.execute(this).
     */
    @Override
    public void execute() {
        getReceiver().execute(this);
    }
}
