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

import java.util.Map;

import org.granite.client.util.WeakIdentityHashMap;

/**
 * @author William DRAI
 */
public class PersistenceManager {
    
    private static Map<Object, EntityManager> entityManagersByEntity = new WeakIdentityHashMap<Object, EntityManager>(1000);
    
    public static EntityManager getEntityManager(Object object) {
        return entityManagersByEntity.get(object);
    }
    
    public static void setEntityManager(Object object, EntityManager entityManager) {
    	if (entityManager == null)
            entityManagersByEntity.remove(object);
    	else
    		entityManagersByEntity.put(object, entityManager);
    }
    
    public static void setPropertyValue(Object object, String propertyName, Object oldValue, Object newValue) {
    	if (newValue == oldValue)
    		return;
    	EntityManager entityManager = getEntityManager(object);
    	if (entityManager != null)
    		entityManager.getTrackingHandler().entityPropertyChangeHandler(object, propertyName, oldValue, newValue);
    }

}
