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
    /** The database URL. */
    private static final String DB_URL = "jdbc:mysql://localhost/scgDB";

    /** The database account name. */
    private static final String DB_ACCOUNT = "student";

    /** The database account password. */
    private static final String DB_PASSWORD = "student";

    DbServer db = new DbServer(DB_URL, DB_ACCOUNT, DB_PASSWORD);

    private Socket connection;
    private List<ClientAccount> clientList;
    private List<Consultant> consultantList;
    private InvoiceServer server;

    private List<TimeCard> timeCardList = new ArrayList<TimeCard>();

    private String outPutDirectoryName = ".";

    private static final Logger logger = Logger.getLogger(CommandProcessor.class.getName());

    public CommandProcessor(Socket connection,
                            List<ClientAccount> clientList,
                            List<Consultant> consultantList,
                            InvoiceServer server) {
        this.connection = connection;
        this.clientList = clientList;
        this.consultantList = consultantList;
        this.server = server;
    }

    /**
     * Set the output directory name.
     * @param outPutDirectoryName - the output directory name.
     */
    public void setOutPutDirectoryName(String outPutDirectoryName) {
        logger.info("Setting output directory to: " + outPutDirectoryName);

        if (!outPutDirectoryName.endsWith("/")) {
            outPutDirectoryName += "/";
        }

        this.outPutDirectoryName = outPutDirectoryName;
    }

    /**
     * execute an AddTimeCardCommand command.
     * @param command - the command to execute.
     */
    public void execute(AddTimeCardCommand command) {

        logger.info("Adding time card");
        TimeCard tc = command.getTarget();
        try {
            db.addTimeCard(tc);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        timeCardList.add(tc);
        //logger.info(tc.toString());
    }

    /**
     * execute an AddClientCommand command.
     * @param command - the command to execute.
     */
    public void execute(AddClientCommand command) {
        logger.info("Adding client " + command.getTarget().getName());
        try {
            db.addClient(command.getTarget());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        clientList.add(command.getTarget());
    }

    /**
     * execute an AddConsultantCommand command.
     * @param command - the command to execute.
     */
    public void execute(AddConsultantCommand command) {
        logger.info("Adding consultant " + command.getTarget().getName());
        try {
            db.addConsultant(command.getTarget());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        consultantList.add(command.getTarget());
    }

    /**
     * Execute an CreateInvoicesCommand command.
     * @param command - the command to execute.
     */
    public void execute(CreateInvoicesCommand command) {
        //should be the same code from previous methods
        logger.info("Creating invoice");
        DbServer db = new DbServer("jdbc:mysql://localhost/scgDB", "student", "student");
        for (ClientAccount client : clientList) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(command.getTarget());
                Invoice invoice = db.getInvoice(client, cal.get(cal.MONTH), 2006);
                String fName = String.format("%s-%s-invoice.txt", client.getName().replace(" ", "_"),
                        Calendar.getInstance().getDisplayName(cal.get(cal.MONTH), Calendar.LONG, Locale.getDefault()));
                final File outputDir = new File(outPutDirectoryName);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                if (!outPutDirectoryName.endsWith("/")) {
                    outPutDirectoryName += "/";
                }
                FileOutputStream fout = new FileOutputStream(outPutDirectoryName + fName, false);
                PrintStream writer = new PrintStream(fout);
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
    public void execute(DisconnectCommand command) {
        logger.info("Disconnecting");
        try {
            connection.shutdownInput();
            connection.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute a ShutdownCommand. Closes any current connections, stops listening for connections and then
     * terminates the server, without calling System.exit.

     * @param command - the input ShutdownCommand.
     */
    public void execute(ShutdownCommand command) {
        logger.info("Shutting down");
        try {
            connection.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            server.shutdown();
        }
    }

    @Override
    public void run() {

    }
}
