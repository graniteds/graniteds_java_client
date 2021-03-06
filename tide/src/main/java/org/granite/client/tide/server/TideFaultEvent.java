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

package org.granite.client.tide.server;

import java.util.Map;
import java.util.concurrent.Future;

import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideFaultEvent extends TideRpcEvent {
    
    private Fault fault;
    private Map<String, Object> extendedData;
    private ServerSession serverSession;
    private ComponentListener<?> componentListener;

    public TideFaultEvent(Context context, ServerSession serverSession, ComponentListener<?> componentListener, Fault fault, Map<String, Object> extendedData) {
        super(context, serverSession, componentListener);
        this.fault = fault;
        this.extendedData = extendedData;
        this.serverSession = serverSession;
        this.componentListener = componentListener;
    }
    
    public int getCallId() {
    	return componentListener.hashCode();
    }
    
    public Fault getFault() {
        return fault;
    }
    
    public void setFault(Fault fault) {
        this.fault = fault;
    }
    
    public Map<String, Object> getExtendedData() {
        return extendedData;
    }
    
    public void setExtendedData(Map<String, Object> extendedData) {
        this.extendedData = extendedData;
    }
    
    public Future<?> retry() {
    	return componentListener.invoke(serverSession);
    }

}
