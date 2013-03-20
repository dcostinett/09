package com.scg.net;

import com.scg.domain.ClientAccount;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 11:37 AM
 * The command to add a Client to a list maintained by the server.
 */
public final class AddClientCommand extends Command<ClientAccount> {

    private static final long serialVersionUID = -698814103304610481L;

    /**
     * Construct an AddClientCommand with a target.
     * @param target - The target of this Command.
     */
    public AddClientCommand(ClientAccount target) {
        super(target);
    }

    /**
     * Execute this Command by calling receiver.execute(this).
     */
    @Override
    public void execute() {
        getReceiver().execute(this);
    }
}
