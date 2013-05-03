package org.granite.client.test.model;

import java.util.Set;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.test.model.embed.Document;
import org.granite.client.test.model.types.DocumentedEntity;
import org.granite.client.test.model.types.NamedEntity;

@RemoteAlias("org.granite.example.addressbook.entity.Person")
@Entity
public class Person extends AbstractEntity implements NamedEntity, DocumentedEntity {

    private static final long serialVersionUID = 1L;

    @RemoteAlias("org.granite.example.addressbook.entity.Person$Salutation")
    public enum Salutation {
        Mr,
        Ms,
        Dr
    }
    
    private Salutation salutation;
    private String firstName;
    private String lastName;
    private Set<Contact> contacts;
    private Contact mainContact;
    private Document document;

    public Salutation getSalutation() {
		return salutation;
	}
	public void setSalutation(Salutation salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Contact> getContacts() {
        return contacts;
    }
    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    public Contact getMainContact() {
        return mainContact;
    }
    public void setMainContact(Contact mainContact) {
        this.mainContact = mainContact;
    }

    public Document getDocument() {
        return document;
    }
    public void setDocument(Document document) {
        this.document = document;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && firstName.length() > 0)
            sb.append(firstName);
        if (lastName != null && lastName.length() > 0) {
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(lastName);
        }
        return sb.toString();
    }
}
