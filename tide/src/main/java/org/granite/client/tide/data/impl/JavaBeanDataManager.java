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

package org.granite.client.tide.data.impl;


/**
 * @author William DRAI
 */
public class JavaBeanDataManager extends AbstractDataManager {

    @Override
    public void setTrackingHandler(TrackingHandler trackingHandler) {
    }

    @Override
    public void startTracking(Object previous, Object parent) {
    }

    @Override
    public void stopTracking(Object previous, Object parent) {
    }

    @Override
    public void clear() {
    }

    public boolean isDirty() {
        return false;
    }

    @Override
    public void notifyDirtyChange(boolean oldDirty, boolean dirty) {    	
    }

    @Override
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity) {
    }
}
