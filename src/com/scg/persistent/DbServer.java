package com.scg.persistent;

import com.scg.domain.*;
import com.scg.util.Address;
import com.scg.util.DateRange;
import com.scg.util.Name;
import com.scg.util.StateCode;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/9/13
 * Time: 11:24 AM
 */
public class DbServer {
    /** connection string to the SQL DB */
    private final String url;

    /** user name for DB credentials */
    private final String username;

    /** DB credentials password */
    private final String password;


    public DbServer(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Clean the db
     */
    public void clean_db() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, username, password);

            stmt = conn.createStatement();
            String sql = String.format("DELETE FROM non_billable_hours; ");
            stmt.executeUpdate(sql);

            sql = String.format("DELETE FROM billable_hours; ");
            stmt.executeUpdate(sql);

            sql = String.format("DELETE FROM timecards; ");
            stmt.executeUpdate(sql);

            sql = String.format("DELETE FROM clients; ");
            stmt.executeUpdate(sql);

            sql = String.format("DELETE FROM consultants");
            stmt.executeUpdate(sql);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Add a client to the database.
     * @param client - the client to add
     * @throws SQLException
     *
     */
    public void addClient(ClientAccount client) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(url, username, password);

            ps = conn.prepareStatement(
                    "INSERT INTO clients (name, street, city, state, postal_code, " +
                    "contact_last_name, contact_first_name, contact_middle_name) " +
                    "VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, client.getName());
            ps.setString(2, client.getAddress().getStreetNumber());
            ps.setString(3, client.getAddress().getCity());
            ps.setString(4, client.getAddress().getState().toString());
            ps.setString(5, client.getAddress().getPostalCode());
            ps.setString(6, client.getContact().getLastName());
            ps.setString(7, client.getContact().getFirstName());
            ps.setString(8, client.getContact().getMiddleName());
            int updateResponse = ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Add a consultant to the database.
     * @param consultant
     * @throws SQLException
     */
    public void addConsultant(Consultant consultant) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
            String sql = String.format(
                    "INSERT into consultants (last_name, first_name, middle_name) " +
                            "VALUES ('%s', '%s', '%s')",
                    consultant.getName().getLastName(),
                    consultant.getName().getFirstName(),
                    consultant.getName().getMiddleName());
            stmt.executeUpdate(sql);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Add a timecard to the database.
     * @param timeCard - the timecard to add
     * @throws SQLException
     *
     * Inserting a new TimeCard in to the database involves four steps:

        1. Obtain the consultant id, for the consultant (using the name fields)
        2. Insert a timecard record providing the consultant id and start date
        3. Obtain the timecard id of the just inserted time card, use 'SELECT LAST_INSERT_ID()'
        4. Insert billable and non-billable hours record for the just created timecard, using the
           timecard id to identify the timecard the hours are associated with

        // Insert non-billable hours
        INSERT INTO non_billable_hours (account_name, timecard_id, date, hours)
        VALUES ('VACATION', 1, '2005/03/13', 8);

        // Insert billable hours
        INSERT INTO billable_hours (client_id, timecard_id, date, skill, hours)
        VALUES ((SELECT DISTINCT id
                FROM clients
                WHERE name = 'Acme Industries'),
                3, '2005/03/12', 'Software Engineer', 8);

    *
     */
    public void addTimeCard(TimeCard timeCard) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();

            String sql = String.format(
                    "SELECT id " +
                    "FROM consultants c " +
                    "WHERE c.last_name='%s' AND c.first_name='%s'",
                    timeCard.getConsultant().getName().getLastName(),
                    timeCard.getConsultant().getName().getFirstName());
            if (timeCard.getConsultant().getName().getMiddleName() != null) {
                sql += String.format(" AND c.middle_name='%s'", timeCard.getConsultant().getName().getMiddleName());
            }
            ResultSet rs = stmt.executeQuery(sql);

            int consultantId = 0;
            if (rs.next()) {
                consultantId = rs.getInt("id");
            }
            rs.close();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT into timecards (consultant_id, start_date) " +
                    "VALUES (?,?)");
            ps.setInt(1, consultantId);
            ps.setDate(2, new java.sql.Date(timeCard.getWeekStartingDay().getTime()));
            ps.executeUpdate();
            ps.close();

            rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
            int timeCardId = 0;
            if (rs.next()) {
                timeCardId = rs.getInt(1);            }
            rs.close();

            List<ConsultantTime> consultingHours = timeCard.getConsultingHours();

            PreparedStatement billablePs = conn.prepareStatement(
                    "INSERT INTO billable_hours (client_id, timecard_id, date, skill, hours) " +
                    "VALUES ((SELECT DISTINCT id " +
                    "FROM clients " +
                    "WHERE name = ?)," +
                    "?,?,?,?)");

            PreparedStatement nonBillablePs = conn.prepareStatement(
                    "INSERT INTO non_billable_hours (account_name, timecard_id, date, hours) " +
                    "VALUES (?,?,?,?)");

            for (ConsultantTime consultantTime : consultingHours) {
                if (consultantTime.isBillable()) {
                    billablePs.setString(1, consultantTime.getAccount().getName());
                    billablePs.setInt(2, timeCardId);
                    billablePs.setDate(3, new Date(consultantTime.getDate().getTime()));
                    billablePs.setString(4, consultantTime.getSkill().toString());
                    billablePs.setInt(5, consultantTime.getHours());
                    billablePs.executeUpdate();
                }
                else {
                    nonBillablePs.setString(1, consultantTime.getAccount().getName());
                    nonBillablePs.setInt(2, timeCardId);
                    nonBillablePs.setDate(3, new Date(consultantTime.getDate().getTime()));
                    nonBillablePs.setInt(4, consultantTime.getHours());
                    nonBillablePs.executeUpdate();
                }
            }

            billablePs.close();
            nonBillablePs.close();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }


    /**
     * Get all of the clients in the database.
     * @return a list of all of the clients
     * @throws SQLException
     */
    public List<ClientAccount> getClients() throws SQLException {
        List<ClientAccount> clients = new ArrayList<ClientAccount>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT name, street, city, state, postal_code, " +
                    "contact_first_name, contact_last_name, contact_middle_name" +
                    " FROM clients c");

            while (rs.next()) {
                String clientName = rs.getString(1);
                String street = rs.getString(2);
                String city = rs.getString(3);
                StateCode state = StateCode.valueOf(rs.getString(4));
                String postalCode = rs.getString(5);
                Name contact = new Name(rs.getString(6), rs.getString(7),rs.getString(8));
                Address address = new Address(street, city, state, postalCode);
                ClientAccount account = new ClientAccount(clientName, contact, address);
                clients.add(account);
            }

            rs.close();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return clients;
    }


    /**
     * Get all of the consultant in the database.
     * @return a list of all of the consultants
     * @throws SQLException
     */
    public List<Consultant> getConsultants() throws SQLException {
        List<Consultant> consultants = new ArrayList<Consultant>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM consultants c");

            while (rs.next()) {
                Name name = new Name(rs.getString(1), rs.getString(2),rs.getString(3));

                Consultant consultant = new Consultant(name);
                consultants.add(consultant);
            }

            rs.close();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return consultants;
    }


    /**
     * Get clients monthly invoice.
     * @param client - the client to obtain the invoice line items for
     * @param month - the month of the invoice
     * @param year - the year of the invoice
     * @return the clients invoice for the month
     * @throws SQLException
     *
     * SELECT b.date, c.last_name, c.first_name, c.middle_name,
        b.skill, s.rate, b.hours
        FROM billable_hours b, consultants c, skills s, timecards t
        WHERE b.client_id = (SELECT DISTINCT id
                            FROM clients
                            WHERE name = 'Acme Industries')
        AND b.skill = s.name
        AND b.timecard_id = t.id
        AND c.id = t.consultant_id
        AND b.date >= '2005/03/01'
        AND b.date <= '2005/03/31';
     */
    public Invoice getInvoice(ClientAccount client, int month, int year) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        Invoice invoice = new Invoice(client, month, year);
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();

            DateRange dr = new DateRange(month, year);

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT b.date, c.last_name, c.first_name, c.middle_name, " +
                            "b.skill, s.rate, b.hours " +
                            "FROM billable_hours b, consultants c, skills s, timecards t " +
                            "WHERE b.client_id = (SELECT DISTINCT id " +
                                    "FROM clients " +
                                    "WHERE name = ?) " +
                            "AND b.skill = s.name " +
                            "AND b.timecard_id = t.id " +
                            "AND c.id = t.consultant_id " +
                            "AND b.date >= ? " +
                            "AND b.date <= ?");
            ps.setString(1, client.getName());
            ps.setDate(2, new Date(dr.getStartDate().getTime()));
            ps.setDate(3, new Date(dr.getEndDate().getTime()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Name name = null;
                if (rs.getString(4) != null && !rs.getString(4).equals("null")) {
                    name = new Name(rs.getString(2), rs.getString(3), rs.getString(4));
                }
                else {
                    name = new Name(rs.getString(2), rs.getString(3));
                }
                Consultant consultant = new Consultant(name);
                ConsultantTime consultantTime =
                        new ConsultantTime(new java.util.Date(rs.getDate(1).getTime()),
                                client, Skill.valueOf(rs.getString(5)), rs.getInt(7));
                TimeCard tc = new TimeCard(consultant, new java.util.Date());
                tc.addConsultantTime(consultantTime);
                invoice.extractLineItems(tc);
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return invoice;
    }


    public String dumpTable(String tableName) throws SQLException {
        StringBuilder sb = new StringBuilder();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s", tableName));

            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                sb.append(rsMetaData.getColumnName(i));
                if (i < rsMetaData.getColumnCount() - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
            while (rs.next()) {
                for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                    sb.append(rs.getString(i));
                    if (i < rsMetaData.getColumnCount() - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\n");
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return sb.toString();
    }

}
