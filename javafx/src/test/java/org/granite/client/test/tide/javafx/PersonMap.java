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

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.collection.javafx.FXPersistentCollections;


@Entity
public class PersonMap extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ReadOnlyMapWrapper<Integer, String> mapSimple = FXPersistentCollections.readOnlyObservablePersistentMap(this, "mapSimple");
    private ReadOnlyMapWrapper<String, EmbeddedAddress> mapEmbed = FXPersistentCollections.readOnlyObservablePersistentMap(this, "mapEmbed");
    private ReadOnlyMapWrapper<String, SimpleEntity> mapEntity = FXPersistentCollections.readOnlyObservablePersistentMap(this, "mapEntity");
    
    
    public PersonMap() {
        super();
    }
    
    public PersonMap(Long id, Long version, String uid, String firstName, String lastName) {
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
    
    public ReadOnlyMapProperty<String, EmbeddedAddress> mapEmbedProperty() {
    	return mapEmbed.getReadOnlyProperty();
    }
    public ObservableMap<String, EmbeddedAddress> getMapEmbed() {
        return mapEmbed.get();
    }
    
    public ReadOnlyMapProperty<Integer, String> mapSimpleProperty() {
    	return mapSimple.getReadOnlyProperty();
    }
    public ObservableMap<Integer, String> getMapSimple() {
        return mapSimple.get();
    }
    
    public ReadOnlyMapProperty<String, SimpleEntity> mapEntityProperty() {
       	return mapEntity.getReadOnlyProperty();
    }    
    public ObservableMap<String, SimpleEntity> getMapEntity() {
        return mapEntity.get();
    }
}
