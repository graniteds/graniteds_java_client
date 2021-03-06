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

package org.granite.client.tide.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.client.tide.Context;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.data.spi.DataManager.TrackingHandler;
import org.granite.client.tide.server.ServerSession;
import org.granite.tide.Expression;

/**
 *  EntityManager is the interface for entity management (!)
 *  It is implemented by the Tide context
 *
 *  @author William DRAI
 */
public interface EntityManager {
    
    /**
     *  Return the entity manager id
     * 
     *  @return the entity manager id
     */
    public String getId();
    
    /**
     *  Return the entity manager state
     * 
     *  @return the entity manager state
     */
    public boolean isActive();
    
    /**
     *  Clear entity cache
     */ 
    public void clearCache();
    
    /**
     *  Clear the current context
     *  Destroys all components/context variables
     */
    public void clear();
    
    public DataManager getDataManager();
    
    public TrackingHandler getTrackingHandler();
    
    /**
     *  Allow uninitialize of persistent collections
     *
     *  @param allowed allow uninitialize of collections
     */
    public void setUninitializeAllowed(boolean allowed);
    
    /**
     *  @return allow uninitialize of collections
     */
    public boolean isUninitializeAllowed();
    
    
    public static interface Propagation {
        
        public void propagate(Object entity, Function func);
    }
    
    public static interface Function {
        
        public void execute(EntityManager entityManager, Object entity);
    }
    
    /**
     *  Setter for the propagation manager
     * 
     *  @param propagation propagation function that will visit child entity managers
     */
    public void setEntityManagerPropagation(Propagation propagation);
    
    /**
     *  Setter for the remote initializer implementation
     * 
     *  @param remoteInitializer instance of IRemoteInitializer
     */
    public void setRemoteInitializer(RemoteInitializer remoteInitializer);
    
    /**
     *  Setter for the remote validator implementation
     * 
     *  @param remoteValidator instance of IRemoteValidator
     */
    public void setRemoteValidator(RemoteValidator remoteValidator);
    
    /**
     *  Create a new temporary entity manager
     * 
     *  @return a temporary entity manager
     */
    public EntityManager newTemporaryEntityManager();
    
    
    /**
     *  Register a reference to the provided object with either a parent or res
     * 
     *  @param entity an entity
     *  @param parent the parent entity
     *  @param propName name of the parent entity property that references the entity
     *  @param expr the context expression
     */ 
    public void addReference(Object entity, Object parent, String propName, Expression expr);
    
    /**
     *  Remove a reference on the provided object
     *
     *  @param entity an entity
     *  @param parent the parent entity to dereference
     *  @param propName name of the parent entity property that references the entity
     *  @param expr expression to remove
     */ 
    public boolean removeReference(Object entity, Object parent, String propName, Expression expr);
    
    /**
     *  Retrieves context expression path for the specified entity (internal implementation)
     *   
     *  @param entity an entity
     *  @param recurse should recurse until 'real' context path, otherwise object reference can be returned
     *  @param cache graph visitor cache
     * 
     *  @return the path from the entity context (or null is no path found)
     */
    public Expression getReference(Object entity, boolean recurse, Set<Object> cache);
    
    /**
     *  Entity manager is dirty when any entity/collection/map has been modified
     *
     *  @return is dirty
     */
    public boolean isDirty();
    
    /**
     *  Entity is dirty when any direct property has been modified
     *  @param entity
     *
     *  @return is dirty
     */
    public boolean isDirtyEntity(Object entity);
    
    /**
     *  Entity is deep dirty when any element in its object graph has been modified
     *  @param entity root of the entity graph
     *
     *  @return is dirty
     */
    public boolean isDeepDirtyEntity(Object entity);
    
    /**
     *  Indicates if the entity is persisted on the server (id/version not null/NaN)
     *
     *  @param entity an entity
     *  @return true if saved
     */
    public boolean isPersisted(Object entity);
    
    /**
     *  Retrieve an entity in the cache from its uid
     *   
     *  @param object an entity
     *  @param nullIfAbsent return null if entity not cached in context
     */
    public Object getCachedObject(Object object, boolean nullIfAbsent);
    
    public Object[] getOwnerEntity(Object object);
    
    public MergeContext initMerge();
    
    public Object mergeExternal(final MergeContext mergeContext, Object obj, Object previous, Expression expr, Object parent, String propertyName, boolean forceUpdate);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals list of entities to remove from the entity manager cache
     *  @param persists list of entities newly persisted to be added in the entity manager cache
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeExternalData(Object obj, Object prev, String externalDataSessionId, List<Object> removals, List<Object> persists);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *	@param serverSession the current server session
     *  @param obj external object
     *
     *  @return merged object
     */
    public Object mergeExternalData(ServerSession serverSession, Object obj);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *	@param serverSession the current server session
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals array of entities to remove from the entity manager cache
     *  @param persists list of entities newly persisted to be added in the entity manager cache
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeExternalData(ServerSession serverSession, Object obj, Object prev, String externalDataSessionId, List<Object> removals, List<Object> persists);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *
     *  @return merged object
     */
    public Object mergeExternalData(Object obj);
    
    // public Object internalMergeExternalData(MergeContext mergeContext, Object obj, Object prev, List<Object> removals);
    
    /**
     *  Merge an object coming from another entity manager (in general in the global context) in the local context
     *
     *  @param sourceEntityManager source context of incoming data
     *  @param obj external object
     *  @param externalDataSessionId is merge from external data
     *  @param uninitializing the merge should uninitialize all collections/entities when possible
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeFromEntityManager(EntityManager sourceEntityManager, Object obj, String externalDataSessionId, boolean uninitializing);
    
    /**
     *  Merge conversation entity manager context variables in global entity manager 
     *  Only applicable to conversation contexts 
     * 
     *  @param entityManager conversation entity manager
     */
    public void mergeInEntityManager(EntityManager entityManager);
    
    /**
     *  Discard changes of entity from last version received from the server
     *
     *  @param entity entity to restore
     */ 
    public void resetEntity(Object entity);
    
    /**
     *  Discard changes of all cached entities from last version received from the server
     */ 
    public void resetAllEntities();
    
    /**
     *  Current map of saved properties for the specified entity
     *  @param entity an entity
     * 
     *  @return saved properties for this entity
     */
    public Map<String, Object> getSavedProperties(Object entity);
    
    
    public static enum UpdateKind {
        PERSIST,
        UPDATE,
        REMOVE,
        REFRESH,
        CONFLICT;
        
        private static final String DATA_EVENT_PREFIX = "org.granite.client.tide.data.";
        
        public static UpdateKind forName(String kind) {
            if ("PERSIST".equals(kind))
                return PERSIST;
            else if ("UPDATE".equals(kind))
                return UPDATE;
            else if ("REMOVE".equals(kind))
                return REMOVE;
            throw new IllegalArgumentException("Unknown update kind " + kind);
        }
        
        public String eventName() {
        	return DATA_EVENT_PREFIX + name().toLowerCase();
        }
        
        public <T> String eventName(Class<T> entityClass) {
        	return DATA_EVENT_PREFIX + name().toLowerCase() + "." + entityClass.getSimpleName();
        }
    }
    
    public static class Update {
        
        private final UpdateKind kind;
        private Object entity;
        
        public Update(UpdateKind kind, Object entity) {
            this.kind = kind;
            this.entity = entity;
        }
        
        public UpdateKind getKind() {
            return kind;
        }
        
        public Object getEntity() {
            return entity;
        }

        public void setEntity(Object entity) {
            this.entity = entity;
        }
        
        public static Update forUpdate(String kind, Object entity) {
            return new Update(UpdateKind.forName(kind), entity);
        }
    }
    
    /**
     *  Handle data updates
     *
     *	@param mergeContext current merge context
     *  @param sourceSessionId sessionId from which data updates come (null when from current session) 
     *  @param updates list of data updates
     */
    public void handleUpdates(MergeContext mergeContext, String sourceSessionId, List<Update> updates);
    
	public void raiseUpdateEvents(Context context, List<EntityManager.Update> updates);
	
    
    public void addListener(DataConflictListener listener);
    
    public void removeListener(DataConflictListener listener);
    
    /**
     *  Accept values for conflict
     * 
     *  @param conflict conflict
     *  @param acceptClient true: keep client changes, false: override with server changes
     */
    public void acceptConflict(Conflict conflict, boolean acceptClient);
    
    /**
     *  Trigger remote initialization of lazy-loaded objects
     * 
     *  @param serverSession current server session
     *  @param entity owner entity
     *  @param propertyName property name
     *  @param object a lazily loaded object
     * 
     *  @return true if initialization triggered
     */
    public boolean initializeObject(ServerSession serverSession, Object entity, String propertyName, Object object);
    
    /**
     *  Trigger remote validation of objects
     * 
     *  @param object an object to remotely validate
     *  @param property a property to validate
     *  @param value value to check
     * 
     *  @return true if validation triggered
     */
    public boolean validateObject(Object object, String property, Object value);
    
    
    public static interface PropagationPolicy {
        
        public void propagate();
    }
}
