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
import org.granite.client.persistence.Lazy;
import org.granite.client.persistence.javafx.PersistentList;


@Entity
public class Classification extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty name = new SimpleStringProperty(this, "name");
    @Lazy
    private ReadOnlyListWrapper<Classification> subclasses = new ReadOnlyListWrapper<Classification>(this, "subclasses", new PersistentList<Classification>());
    @Lazy
    private ReadOnlyListWrapper<Classification> superclasses = new ReadOnlyListWrapper<Classification>(this, "superclasses", new PersistentList<Classification>());
    
    
    public Classification() {
        super();
    }
    
    public Classification(Long id, Long version, String uid, String name) {
        super(id, version, uid);
        this.name.set(name);
    }
    
    public Classification(Long id, boolean initialized, String detachedState) {
        super(id, initialized, detachedState);
    }
    
    public StringProperty nameProperty() {
        return name;
    }    
    public String getName() {
        return name.get();
    }    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public ReadOnlyListProperty<Classification> subclassesProperty() {
    	return subclasses.getReadOnlyProperty();
    }
    public ObservableList<Classification> getSubclasses() {
        return subclasses.get();
    }
    
    public ReadOnlyListProperty<Classification> superclassesProperty() {
    	return superclasses.getReadOnlyProperty();
    }
    public ObservableList<Classification> getSuperclasses() {
        return superclasses.get();
    }
}
