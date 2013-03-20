package com.scg.net.server;

import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.net.Command;
import com.scg.net.ShutdownCommand;
import com.scg.persistent.DbServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 11:46 AM
 *
 *
     1. Initialize the CommandProcessor, which will need to have access to the client and consultant lists
        (from ListFactory). The timecard list will be populated with TimeCards by receiving them from the client.
     2. Set up a ServerSocket on localhost (127.0.0.1), port 10888 and wait for a client to connect.
     3. When the client connects, set up and ObjectInputStream for the connection, and start reading Command objects.
     4. When a Command is received, the server sets its receiver to the CommandProcessor, and calls the execute method
        of the Command, then loops back to read more Command objects.
     5. In its execute method, the Command object calls the receiver's execute method, with itself (this) as the
        parameter.
     6. The CommandProcessor executes the Command in the execute method that is called. Processing of the add commands
        should simply add the target object to the appropriate list. In the case of the CreateInvoicesCommand, the
        invoice should be created and the invoice text should be written to a file whose name is of the form:
        <ClientName>-<MonthName>-Invoice.txt. Please do not leave any spaces in the file name. In the case of the
        DisconnectCommand the server should cease reading commands and close the connection.
     7. Once the client disconnects the server should resume waiting for a connection.
     8. The ShutdownCommand should cause the server to terminate in an orderly fashion, without invoking System.exit().
        This should be performed on a separate connection.
 */
public class InvoiceServer implements Runnable {
    private int port;
    private List<ClientAccount> clientList;
    private List<Consultant> consultantList;

    private ServerSocket servSock = null;

    private boolean isShutdownCommand = false;

    private static final Logger logger = Logger.getLogger(InvoiceServer.class.getName());

    /**
     * Construct an InvoiceServer with a port.
     * @param port - The port for this server to listen on
     * @param clientList - the initial list of clients
     * @param consultantList - the initial list of consultants
     */
    public InvoiceServer(int port, List<ClientAccount> clientList, List<Consultant> consultantList) {
        this.port = port;
        this.clientList = clientList;
        this.consultantList = consultantList;
    }

    /**
     * Run this server, establishing connections, receiving commands, and sending them to the CommandProcesser.
     */
    public void run() {
        try {
            servSock = new ServerSocket(port);
            while (!isShutdownCommand) {
                logger.info("Server ready on port " + port + " ...");
                Socket sock = servSock.accept(); // blocks

                CommandProcessor proc = new CommandProcessor(sock, clientList, consultantList, this);

                if (sock.getInputStream().available() <= 0) {
                    continue;
                }
                ObjectInputStream iStream = new ObjectInputStream(sock.getInputStream());

                //check for a valid command object before casting
                Command cmd = (Command) iStream.readObject();
                while (cmd != null) {
                    cmd.setReceiver(proc);
                    cmd.execute();
                    if (!sock.isClosed()) {
                        cmd = (Command) iStream.readObject();
                    } else {
                        cmd = null;
                    }
                }
/*
                Thread t = new Thread(proc);
                t.start();
*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (servSock != null) {
                try {
                    servSock.close();
                } catch (IOException ioex) {
                    System.err.println("Error closing server socket. " + ioex);
                }
            }
        }
    }

    /**
     * Shutdown the server
     */
    void shutdown() {
        isShutdownCommand = true;
        if (servSock != null) {
            try {
                if (!servSock.isClosed()) {
                    servSock.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
