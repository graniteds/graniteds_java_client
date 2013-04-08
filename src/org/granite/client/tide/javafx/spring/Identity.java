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

package org.granite.client.tide.javafx.spring;

import java.util.HashMap;
import java.util.Map;


import org.granite.client.tide.javafx.BaseIdentity;
import org.granite.client.tide.javafx.ObservablePermission;
import org.granite.client.tide.javafx.ObservableRole;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.util.WeakIdentityHashMap;
import org.granite.messaging.amf.RemoteClass;

/**
 * @author William DRAI
 */
@RemoteClass("org.granite.tide.spring.security.Identity")
public class Identity extends BaseIdentity {
	
    protected Identity() {
    	// CDI proxying...
    }
    
    public Identity(final ServerSession serverSession) {
    	super(serverSession);
    }
        
    
    private Map<String, ObservableRole> ifAllGrantedCache = new HashMap<String, ObservableRole>();
    private Map<String, ObservableRole> ifAnyGrantedCache = new HashMap<String, ObservableRole>();
    private Map<String, ObservableRole> ifNotGrantedCache = new HashMap<String, ObservableRole>();
    
    
    public ObservableRole hasRole(String roleName) {
    	return ifAllGranted(roleName);
    }
    
    public ObservableRole ifAllGranted(String roleName) {
    	ObservableRole role = ifAllGrantedCache.get(roleName);
    	if (role == null) {
    		role = new ObservableRole(Identity.this, getContext(), getServerSession(), "ifAllGranted", roleName);
    		ifAllGrantedCache.put(roleName, role);
    	}
    	return role;
    }
    
    public ObservableRole ifAnyGranted(String roleName) {
    	ObservableRole role = ifAnyGrantedCache.get(roleName);
    	if (role == null) {
    		role = new ObservableRole(Identity.this, getContext(), getServerSession(), "ifAnyGranted", roleName);
    		ifAnyGrantedCache.put(roleName, role);
    	}
    	return role;
    }
    
    public ObservableRole ifNotGranted(String roleName) {
    	ObservableRole role = ifNotGrantedCache.get(roleName);
    	if (role == null) {
    		role = new ObservableRole(Identity.this, getContext(), getServerSession(), "ifNotGranted", roleName);
    		ifNotGrantedCache.put(roleName, role);
    	}
    	return role;
    }

    private Map<Object, Map<String, ObservablePermission>> permissionsCache = new WeakIdentityHashMap<Object, Map<String, ObservablePermission>>();
    
    public ObservablePermission hasPermission(Object entity, String action) {
    	Map<String, ObservablePermission> entityPermissions = permissionsCache.get(entity);
    	if (entityPermissions == null) {
    		entityPermissions = new HashMap<String, ObservablePermission>();
    		permissionsCache.put(entity, entityPermissions);
    	}
    	ObservablePermission permission = entityPermissions.get(action);
    	if (permission == null) {
    		permission = new ObservablePermission(this, getContext(), getServerSession(), "hasPermission", entity, action);
    		entityPermissions.put(action, permission);
    	}
    	return permission;
    }

    @Override
    protected void initSecurityCache() {
    	for (ObservableRole role : ifAllGrantedCache.values())
    		role.clear();
    	for (ObservableRole role : ifAnyGrantedCache.values())
    		role.clear();
    	for (ObservableRole role : ifNotGrantedCache.values())
    		role.clear();
    	
    	for (Map<String, ObservablePermission> entityPermissions : permissionsCache.values()) {
    		for (ObservablePermission permission : entityPermissions.values())
    			permission.clear();
    	}
    }
    
    /**
     * 	Clear the security cache
     */
    @Override
    public void clearSecurityCache() {
    	for (ObservableRole role : ifAllGrantedCache.values())
    		role.clear();
    	ifAllGrantedCache.clear();
    	for (ObservableRole role : ifAnyGrantedCache.values())
    		role.clear();
    	ifAnyGrantedCache.clear();
    	for (ObservableRole role : ifNotGrantedCache.values())
    		role.clear();
    	ifNotGrantedCache.clear();
    	
    	for (Map<String, ObservablePermission> entityPermissions : permissionsCache.values()) {
    		for (ObservablePermission permission : entityPermissions.values())
    			permission.clear();
    	}
    	permissionsCache.clear();
    }
}

