package org.granite.client.tide.collections;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.server.ServerSession;


public interface ManagedPersistentAssociation extends LazyableCollection {
    
    public Object getOwner();
    
    public String getPropertyName();
    
    public LazyableCollection getCollection();
    
    public void setServerSession(ServerSession serverSession);
    
    public void addListener(InitializationListener listener);
    
    public interface InitializationListener {
        
        public void initialized(ManagedPersistentAssociation collection);
        
        public void uninitialized(ManagedPersistentAssociation collection);
    }
    
    public interface InitializationCallback {
        
        public void call(ManagedPersistentAssociation collection);
    }
}