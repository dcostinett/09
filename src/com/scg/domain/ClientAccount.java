package com.scg.domain;

import com.scg.util.Address;
import com.scg.util.Name;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 1/12/13
 * Time: 5:20 PM
 */
@SuppressWarnings({"serial", "unchecked"})
public class ClientAccount implements Account, Comparable<ClientAccount>, Serializable {


    /**
     * String with the name of the client,
     */
    private final String name;

    /**
     * Name of the contact person for this client.
     */
    private Name contact;

    /**
     * Client's mailing address
     */
    private Address address;

    public ClientAccount(final String name, final Name contact) {
        this.name = name;
        this.contact = contact;
    }

    /**
     * Creates a new instance of ClientAccount
     * @param name - String with the name of the client.
     * @param contact - Name of the contact person for this client.
     * @param address - Address of this client.
     */
    public ClientAccount(String name, Name contact, Address address) {
        this.name = name;
        this.contact = contact;
        this.address = address;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isBillable() {
        return true;
    }

    public Name getContact() {
        return contact;
    }

    public void setContact(final Name contact) {
        this.contact = contact;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    @Override
    public int compareTo(final ClientAccount o) {
        return name.compareTo(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientAccount)) return false;

        ClientAccount that = (ClientAccount) o;

        if (!address.equals(that.address)) return false;
        if (!contact.equals(that.contact)) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + contact.hashCode();
        result = 31 * result + address.hashCode();
        return result;
    }

    /**
     * String representation for this Client.
     *
     * @return Formatted string of the form
     *
     * <pre>
     *  Client Name
     *  Client Address
     *  Client Contact Name
     * </pre>
     */
    @Override
    public String toString() {
        return String.format("%s%n%s%n%s%n", name, address.toString(), contact.toString());
    }


}


