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

package org.granite.client.tide.collections;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentCollection.InitializationCallback;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.server.ServerSession;


/**
 *  Internal implementation of persistent collection handling automatic lazy loading.<br/>
 *  Used for wrapping persistent collections received from the server.<br/>
 *  Should not be used directly.
 * 
 *  @author William DRAI
 */
public class CollectionLoader implements Loader<PersistentCollection> {
    
    private final ServerSession serverSession;
    
    private final Identifiable entity;
    private final String propertyName;
    
    private boolean localInitializing = false;
    private boolean initializing = false;
    private InitializationCallback initializationCallback = null;
    
    
	public CollectionLoader(ServerSession serverSession, Identifiable entity, String propertyName) {
    	this.serverSession = serverSession;
        this.entity = entity;
        this.propertyName = propertyName;
    }
    
    public boolean isInitializing() {
        return initializing;
    }
    
    public void onInitializing() {
        localInitializing = true;
    }
    
    public void onInitialize() {
    	localInitializing = false;
    }
    
    public void onUninitialize() {
        initializing = false;
        localInitializing = false;
        initializationCallback = null;
    }
    
    public void load(PersistentCollection collection, InitializationCallback callback) {
        if (localInitializing)
            return;
        
        this.initializationCallback = callback;
        
        EntityManager entityManager = PersistenceManager.getEntityManager(entity);
        if (!initializing && entityManager.initializeObject(serverSession, collection))                
            initializing = true;
    }
}