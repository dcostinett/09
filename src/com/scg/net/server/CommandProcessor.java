package com.scg.net.server;

import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.domain.Invoice;
import com.scg.domain.TimeCard;
import com.scg.net.*;
import com.scg.persistent.DbServer;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 11:43 AM
 *
 * The command processor for the invoice server. Implements the receiver role in the Command design pattern.
 *
 * The CommandProcessor executes the Command in the execute method that is called. Processing of the add commands
 * should simply add the target object to the appropriate list. In the case of the CreateInvoicesCommand, the invoice
 * should be created and the invoice text should be written to a file whose name is of the form:
 * <ClientName>-<MonthName>-Invoice.txt. Please do not leave any spaces in the file name. In the case of the
 * DisconnectCommand the server should cease reading commands and close the connection.
 */
public class CommandProcessor implements Runnable {
    private static final Logger logger = Logger.getLogger(CommandProcessor.class.getName());

    /** The database URL. */
    private static final String DB_URL = "jdbc:mysql://localhost/scgDB";

    /** The database account name. */
    private static final String DB_ACCOUNT = "student";

    /** The database account password. */
    private static final String DB_PASSWORD = "student";

    private final DbServer db = new DbServer(DB_URL, DB_ACCOUNT, DB_PASSWORD);

    private final ThreadLocal<Socket> clientSocket = new ThreadLocal<Socket>();
    private final ThreadLocal<List<ClientAccount>> clientList = new ThreadLocal<List<ClientAccount>>();
    private final ThreadLocal<List<Consultant>> consultantList = new ThreadLocal<List<Consultant>>();
    private final ThreadLocal<InvoiceServer> server = new ThreadLocal<InvoiceServer>();

    private final ThreadLocal<List<TimeCard>> timeCardList = new ThreadLocal<List<TimeCard>>();

    private String outPutDirectoryName = ".";

    public CommandProcessor(final Socket connection,
                            final List<ClientAccount> clientList,
                            final List<Consultant> consultantList,
                            final InvoiceServer server) {
        this.clientSocket.set(connection);
        this.clientList.set(clientList);
        this.consultantList.set(consultantList);
        this.server.set(server);
    }

    /**
     * Set the output directory name.
     * @param dir - the output directory name.
     */
    public void setOutPutDirectoryName(final String dir) {
        logger.info("Setting output directory to: " + dir);

        this.outPutDirectoryName = dir;
        if (!outPutDirectoryName.endsWith("/")) {
            outPutDirectoryName += "/";
        }
    }

    /**
     * execute an AddTimeCardCommand command.
     * @param command - the command to execute.
     */
    public void execute(final AddTimeCardCommand command) {

        logger.info("Adding time card");
        final TimeCard tc = command.getTarget();
        try {
            db.addTimeCard(tc);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        timeCardList.add(tc);
        //logger.info(tc.toString());
    }

    /**
     * execute an AddClientCommand command.
     * @param command - the command to execute.
     */
    public void execute(final AddClientCommand command) {
        logger.info("Adding client " + command.getTarget().getName());
        try {
            db.addClient(command.getTarget());
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        clientList.add(command.getTarget());
    }

    /**
     * execute an AddConsultantCommand command.
     * @param command - the command to execute.
     */
    public void execute(final AddConsultantCommand command) {
        logger.info("Adding consultant " + command.getTarget().getName());
        try {
            db.addConsultant(command.getTarget());
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        consultantList.add(command.getTarget());
    }

    /**
     * Execute an CreateInvoicesCommand command.
     * @param command - the command to execute.
     */
    public void execute(final CreateInvoicesCommand command) {
        //should be the same code from previous methods
        logger.info("Creating invoice");
        for (ClientAccount client : clientList.get()) {
            try {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(command.getTarget());
                final Invoice invoice = db.getInvoice(client, cal.get(cal.MONTH), 2006);
                final String fName = String.format("%s-%s-invoice.txt", client.getName().replace(" ", "_"),
                        Calendar.getInstance().getDisplayName(cal.get(cal.MONTH), Calendar.LONG, Locale.getDefault()));
                final File outputDir = new File(outPutDirectoryName);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                if (!outPutDirectoryName.endsWith("/")) {
                    outPutDirectoryName += "/";
                }
                final FileOutputStream fout = new FileOutputStream(outPutDirectoryName + fName, false);
                final PrintStream writer = new PrintStream(fout);
                writer.print(invoice);
                writer.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Execute an DisconnectCommand command.
     * @param command - the command to execute.
     */
    public void execute(final DisconnectCommand command) {
        logger.info("Disconnecting");
        try {
            clientSocket.get().shutdownInput();
            clientSocket.get().close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute a ShutdownCommand. Closes any current connections, stops listening for connections and then
     * terminates the server, without calling System.exit.

     * @param command - the input ShutdownCommand.
     */
    public void execute(final ShutdownCommand command) {
        logger.info("Shutting down");
        try {
            clientSocket.get().close();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            server.get().shutdown();
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream iStream = new ObjectInputStream(clientSocket.get().getInputStream());
            while (!clientSocket.get().isClosed()) {
                final Object obj = iStream.readObject();
                if (!(obj instanceof Command)) {
                    logger.warning("Unable to create Command object from: " + obj.toString());
                    continue;
                }
                final Command cmd = (Command) obj;
                cmd.setReceiver(this);
                cmd.execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
