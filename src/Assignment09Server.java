import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.net.Command;
import com.scg.net.server.CommandProcessor;
import com.scg.net.server.InvoiceServer;
import com.scg.persistent.DbServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 11:26 AM
 *
 * The server program, Assignment09Server, will perform the following:

     1. Initialize the CommandProcessor, which will need to have access to the client and consultant lists
        (from ListFactory). The timecard list will be populated with TimeCards by receiving them from the client.

     2. Set up a ServerSocket on localhost (127.0.0.1), port 10888 and wait for a client to connect.

     3. When the client connects, set up an ObjectInputStream for the connection, and start reading Command objects.

     4. When a Command is received, the server sets its receiver to the CommandProcessor, and calls the execute method
        of the  Command, then loops back to read more Command objects.

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
public class Assignment09Server {
    /** The port for the server to listen on.*/
    public static final int DEFAULT_PORT = 10888;

    /**
     * Instantiates an InvoiceServer,initializes its account and consultant lists and starts it.
     * @param args - Command line parameters.
     * @throws - if the server raises any exceptions.
     */
    public static void main(String[] args) throws Exception {

        List<ClientAccount> clients = new ArrayList<ClientAccount>(); //db.getClients();
        List<Consultant> consultants = new ArrayList<Consultant>();   //db.getConsultants();
        ListFactory.populateClientList(clients);
        ListFactory.populateConsultantList(consultants);

        InvoiceServer server = new InvoiceServer(DEFAULT_PORT, clients, consultants);

        server.run();
    }
}
