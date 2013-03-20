import com.scg.domain.ClientAccount;
import com.scg.domain.Consultant;
import com.scg.domain.TimeCard;
import com.scg.persistent.DbServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/9/13
 * Time: 1:18 PM
 *
 * Initialize/populate the DB
 */
public class InitDb {

    /**
     *
     * @param args - not used
     * @throws Exception - if anything goes awry
     */
    public static void main(String[] args) throws Exception {
        // Create lists to be populated by factory
        List<ClientAccount> accounts = new ArrayList<ClientAccount>();
        List<Consultant> consultants = new ArrayList<Consultant>();
        List<TimeCard> timeCards = new ArrayList<TimeCard>();
        ListFactory.populateLists(accounts, consultants, timeCards);

        DbServer db = new DbServer("jdbc:mysql://localhost/scgDB", "student", "student");
        db.clean_db();
        for (ClientAccount account : accounts) {
            db.addClient(account);
        }

        for (Consultant consultant : consultants) {
            db.addConsultant(consultant);
        }

        for (TimeCard tc : timeCards) {
            //db.addTimeCard(tc);
        }
    }
}
