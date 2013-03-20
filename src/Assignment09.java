import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.domain.TimeCard;
import com.scg.net.AddClientCommand;
import com.scg.net.AddConsultantCommand;
import com.scg.net.AddTimeCardCommand;
import com.scg.net.client.InvoiceClient;
import com.scg.persistent.DbServer;
import com.scg.util.Name;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
 * The client application for Assignment09.
 *
    The client program, Assignment09, will perform the following:

    1. Create the time card list as before (using ListFactory).
    2. Set up a network connection with the Assignment09Server running on localhost port 10888.
    3. Create an ObjectOutputStream for the connection.
    4. Send several new Consultants (consultants not used in the timecards/invoices) to the server encapsulated in an
        AddConsultantCommand object.
    5. Send several new Clients (clients not used in the timecards/invoices) to the server encapsulated in an
        AddClientCommand object.
    6. Send the TimeCards to the server, each encapsulated in an AddTimeCardCommand object.
    7. Send a CreateInvoicesCommand to the server, with the month for which invoices are to be created encapsulated in
        a CreateInvoicesCommand object.
    8. Send the DisconnectCommand to the server, and close the connection.
    9. Create a new connection to the server, send the ShutdownCommand to the server, and close the connection and exit.
 *
 */
public class Assignment09 {

    public static final int DEFAULT_PORT = 10888;

    private static Socket socket;
    /**
     * Instantiates an InvoiceClient, provides it with a set of timecards to server the server and starts it running.
     * @param args - Command line parameters, not used.
     */
    public static void main(@SuppressWarnings("unused") String[] args) {

        DbServer db = new DbServer("jdbc:mysql://localhost/scgDB", "student", "student");

        List<ClientAccount> clients = new ArrayList<ClientAccount>();
        List<Consultant> consultants = new ArrayList<Consultant>();
        List<TimeCard> timeCards = new ArrayList<TimeCard>();
        ListFactory.populateLists(clients,  consultants, timeCards);

        try {
            db.clean_db();
            for (ClientAccount client : clients) {
                db.addClient(client);
            }
            for (Consultant consultant : consultants){
                db.addConsultant(consultant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        InvoiceClient invoiceClient = new InvoiceClient("localhost", DEFAULT_PORT, timeCards);
        invoiceClient.run();

        InvoiceClient shutdownClient = new InvoiceClient("localhost", DEFAULT_PORT, timeCards);
        shutdownClient.sendQuit();
    }
}
