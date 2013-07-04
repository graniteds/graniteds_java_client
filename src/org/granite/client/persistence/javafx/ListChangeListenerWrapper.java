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

package org.granite.client.persistence.javafx;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.client.persistence.collection.PersistentCollection;

/**
 * @author William DRAI
 */
public class ListChangeListenerWrapper<T> implements ListChangeListener<T> {
    private final ObservableList<T> wrappedList;
    private final ListChangeListener<T> wrappedListener;
    
    public ListChangeListenerWrapper(ObservableList<T> wrappedList, ListChangeListener<T> wrappedListener) {
        this.wrappedList = wrappedList;
        this.wrappedListener = wrappedListener;
    }
    
    @Override
    public void onChanged(ListChangeListener.Change<? extends T> change) {
        if (!((PersistentCollection)wrappedList).wasInitialized())
            return;
        ListChangeListener.Change<T> wrappedChange = new ListChangeWrapper<T>(wrappedList, change);
        wrappedListener.onChanged(wrappedChange);
    }        
}