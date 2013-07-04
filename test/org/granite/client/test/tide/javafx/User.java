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

import java.io.Serializable;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;


@Entity
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
	private boolean __initialized__ = true;
    @SuppressWarnings("unused")
    private String __detachedState__ = null;

    @Id
    private final ReadOnlyStringWrapper username = new ReadOnlyStringWrapper(this, "username");
    private final StringProperty name = new SimpleStringProperty(this, "name");


    public User() {        
    }
    
    public User(String username, boolean initialized, String detachedState) {
        if (!initialized) {
            __initialized__ = false;
            __detachedState__ = detachedState;
        }
        else
        	this.username.set(username);
    }
    
    public User(String username) {
        this.username.set(username);
    }
    
    public StringProperty usernameProperty() {
        return username;
    }
    
    public String getUsername() {
        return username.get();
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
}
