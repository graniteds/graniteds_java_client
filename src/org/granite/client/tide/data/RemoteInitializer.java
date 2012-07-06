package org.granite.client.tide.data;

import org.granite.client.tide.server.ServerSession;


public interface RemoteInitializer {
    
    public void setEnabled(boolean enabled);

    public boolean isEnabled();
    
    /**
     *  Trigger remote initialization of an object
     *
     *  @param object a lazy loaded object
     */
    public boolean initializeObject(ServerSession serverSession, Object object);

}