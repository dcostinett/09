package com.scg.net;

import com.scg.domain.Consultant;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 12:08 PM
 */
public class AddConsultantCommand extends Command<Consultant> {
    private static final long serialVersionUID = -5579394019616828436L;

    /**
     *
     * @param target - the target
     */
    public AddConsultantCommand(Consultant target) {
        super(target);
    }

    @Override
    public void execute() {
        getReceiver().execute(this);
    }
}
