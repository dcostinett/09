package com.scg.net;

import com.scg.domain.TimeCard;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 11:59 AM
 *
 * The command to add a Consultant to a list maintained by the server.
 */
public final class AddTimeCardCommand extends Command<TimeCard> {
    private static final long serialVersionUID = -1096168261168597249L;

    /**
     * Construct an AddTimeCardCommand with a target.
     * @param target - The target of this Command.
     */
    public AddTimeCardCommand(TimeCard target) {
        super(target);
    }

    @Override
    public void execute() {
        getReceiver().execute(this);
    }
}
