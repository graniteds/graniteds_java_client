/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.test.tide.javafx;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.collection.javafx.FXPersistentCollections;


@Entity
public class Person2 extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private final StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private final StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private final ReadOnlyListWrapper<String> names = FXPersistentCollections.readOnlyObservablePersistentList(this, "names");
    private final ReadOnlyListWrapper<Contact> contacts = FXPersistentCollections.readOnlyObservablePersistentList(this, "contacts");
    
    
    public Person2() {
        super();
    }
    
    public Person2(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public StringProperty firstNameProperty() {
        return firstName;
    }    
    public String getFirstName() {
        return firstName.get();
    }    
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }    
    public String getLastName() {
        return lastName.get();
    }    
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    
    public ReadOnlyListProperty<String> namesProperty() {
    	return names.getReadOnlyProperty();
    }
    public ObservableList<String> getNames() {
        return names.get();
    }
    
    public ReadOnlyListProperty<Contact> contactsProperty() {
    	return contacts.getReadOnlyProperty();
    }
    public ObservableList<Contact> getContacts() {
        return contacts.get();
    }
}
