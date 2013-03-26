package com.scg.net.server;

import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.domain.Invoice;
import com.scg.domain.TimeCard;
import com.scg.net.*;
import com.scg.persistent.DbServer;
import com.scg.util.DateRange;
import com.scg.util.TimeCardListUtil;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
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

    private final Socket clientSocket;
    private final List<ClientAccount> clientList;
    private final List<Consultant> consultantList;
    private final InvoiceServer server;

    private final List<TimeCard> timeCardList = new ArrayList<TimeCard>();

    private String outputDirectoryName = ".";

    private final Object lock = new Object();

    //pass in a timeCardList instance in order to aggregate all the data into one list
    public CommandProcessor(final Socket connection,
                            final List<ClientAccount> clientList,
                            final List<Consultant> consultantList,
                            final InvoiceServer server) {
        this.clientSocket = connection;
        this.clientList = clientList;
        this.consultantList = consultantList;
        this.server = server;
    }

    /**
     * Set the output directory name.
     * @param dir - the output directory name.
     */
    public void setOutPutDirectoryName(final String dir) {
        logger.info("Setting output directory to: " + dir);

        this.outputDirectoryName = dir;
        if (!outputDirectoryName.endsWith("/")) {
            outputDirectoryName += "/";
        }
    }

    /**
     * execute an AddTimeCardCommand command.
     * @param command - the command to execute.
     */
    public void execute(final AddTimeCardCommand command) {

        logger.info("Adding time card");
        final TimeCard tc = command.getTarget();

        synchronized (timeCardList) {
            if (!timeCardList.contains(tc)) {
                timeCardList.add(tc);
            }
        }
    }

    /**
     * execute an AddClientCommand command.
     * @param command - the command to execute.
     */
    public void execute(final AddClientCommand command) {
        logger.info("Adding client " + command.getTarget().getName());

        synchronized (clientList) {
            if (!clientList.contains(command.getTarget())) {
                clientList.add(command.getTarget());
            }
        }
    }

    /**
     * execute an AddConsultantCommand command.
     * @param command - the command to execute.
     */
    public void execute(final AddConsultantCommand command) {
        logger.info("Adding consultant " + command.getTarget().getName());

        synchronized (consultantList) {
            if (!consultantList.contains(command.getTarget())) {
                consultantList.add(command.getTarget());
            }
        }
    }

    /**
     * Execute an CreateInvoicesCommand command.
     * @param command - the command to execute.
     */
    public void execute(final CreateInvoicesCommand command) {
/*
        //should be the same code from previous methods
        logger.info("Creating invoice");
        for (ClientAccount client : clientList) {
            try {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(command.getTarget());
                final Invoice invoice = db.getInvoice(client, cal.get(Calendar.MONTH), 2006);
                final String fName = String.format("%s-%s-invoice.txt", client.getName().replace(" ", "_"),
                        Calendar.getInstance().getDisplayName(cal.get(Calendar.MONTH), Calendar.LONG, Locale.getDefault()));
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
*/
        logger.info("Executing invoice command.");
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(command.getTarget());

        final SimpleDateFormat formatter = new SimpleDateFormat("MMMMyyyy");
        final String monthString = formatter.format(calendar.getTime());
        synchronized (clientList) {
            for (final ClientAccount client : clientList) {
                Invoice invoice = new Invoice(client,
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.YEAR));

                List<TimeCard> timeCardListForClient;
                timeCardListForClient = TimeCardListUtil.getTimeCardsForDateRange(timeCardList,
                        new DateRange(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));

                for (final TimeCard currentTimeCard : timeCardList) {
                    invoice.extractLineItems(currentTimeCard);
                }

                final File serverDir = new File(outputDirectoryName);
                if (!serverDir.exists()) {
                    if (!serverDir.mkdirs()) {
                        logger.severe("Unable to create directory, " + serverDir.getAbsolutePath());
                        return;
                    }
                }
                final String outFileName = String.format("%s%s%sInvoice.txt",
                        outputDirectoryName,
                        client.getName().replaceAll(" ", "_"),
                        monthString);

                PrintStream printOut = null;
                try {
                    printOut = new PrintStream(new FileOutputStream(outFileName), true);
                    printOut.println(invoice.toString());
                } catch (final FileNotFoundException e) {
                    logger.log(Level.SEVERE, "Can't open file " + outFileName, e);
                } finally {
                    if (printOut != null) {
                        printOut.close();
                    }
                }
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
            clientSocket.shutdownInput();
            clientSocket.close();
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
            clientSocket.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            server.shutdown();
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream iStream = new ObjectInputStream(clientSocket.getInputStream());
            while (!clientSocket.isClosed()) {
                final Object obj = iStream.readObject();
                if (!(obj instanceof Command)) {
                    logger.warning("Unable to create Command object from: " + obj.toString());
                    continue;
                }
                final Command<?> cmd = (Command<?>) obj;
                cmd.setReceiver(this);
                cmd.execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //should add a finally clause that closes socket and iStream if there was a problem.
    }
}
