/*
 * Created on Mar 6, 2007
 */

public class Commander {

    /**
     * Demonstrate use of commands.
     *
     * @param args - unused
     */
    public static void main(final String[] args) {
        // client creates the commands...
        final Command on = new OnCommand();
        final Command off = new OffCommand();
        
        // The server does the rest...
        final Receiver r = new Receiver();
        
        final Command commands[] = {on, off};
        // Accept the command and process it
        for (final Command cmd : commands) {
            cmd.setReceiver(r);
            cmd.execute();
        }
    }

///////////////////////////////////////////////////////////////////////////////
// The classes/interface below this point would normally be separate files.  //
///////////////////////////////////////////////////////////////////////////////

    /**
     * The command interface
     */
    interface Command {
        void execute();
        void setReceiver(Receiver r);
    }
    
    /** The receiver. */
    public static class Receiver {
        public void action(@SuppressWarnings("unused") final OnCommand cmd) {
            System.out.println("Do the ON action. ");
        }
        public void action(@SuppressWarnings("unused") final OffCommand cmd) {
            System.out.println("Do the OFF action.");
        }
    }

    /**
     * A concrete command.
     */
    public static class OnCommand implements Command {
        Receiver recvr;
        @Override
        public void setReceiver(final Receiver recvr) {
            this.recvr = recvr;
        }
        @Override
        public void execute() {
            recvr.action(this);
        }
    }

    /**
     * Another concrete command.
     */
    public static class OffCommand implements Command {
        Receiver recvr;
        @Override
        public void setReceiver(final Receiver recvr) {
            this.recvr = recvr;
        }
        @Override
        public void execute() {
            recvr.action(this);
        }
    }

}
