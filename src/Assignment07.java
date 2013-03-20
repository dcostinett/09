import com.scg.domain.ClientAccount;
import com.scg.domain.Invoice;
import com.scg.persistent.DbServer;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/10/13
 * Time: 9:37 AM
 */
public class Assignment07 {

    /** The start month for our test cases. */
    private static final int INVOICE_MONTH = Calendar.MARCH;

    /** The test year. */
    private static final int INVOICE_YEAR = 2006;

    /** The database URL. */
    private static final String DB_URL = "jdbc:mysql://localhost/scgDB";

    /** The database account name. */
    private static final String DB_ACCOUNT = "student";

    /** The database account password. */
    private static final String DB_PASSWORD = "student";


    public static void main(String[] args) {
        DbServer db = new DbServer(DB_URL, DB_ACCOUNT, DB_PASSWORD);
        try {
            List<ClientAccount> clients = db.getClients();

            for (ClientAccount client : clients) {
                Invoice invoice = db.getInvoice(client, Calendar.FEBRUARY, INVOICE_YEAR);
                System.out.println(invoice);
                invoice = db.getInvoice(client, INVOICE_YEAR, INVOICE_YEAR);
                System.out.println(invoice);
                invoice = db.getInvoice(client, Calendar.APRIL, INVOICE_YEAR);
                System.out.println(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
