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

package org.granite.client.messaging.messages;

import java.io.Externalizable;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public interface Message extends Externalizable, Cloneable {

	public static enum Type {
		
		// Request types.
		PING,
		LOGIN,
		LOGOUT,
		INVOCATION,
		SUBSCRIBE,
		UNSUBSCRIBE,
		PUBLISH,
		
		// Response types.
		RESULT,
		FAULT,
		
		// Push types.
		TOPIC,
		DISCONNECT
	}
    
    Type getType();
	
	String getId();
    void setId(String id);
	
	String getClientId();
    void setClientId(String clientId);

	long getTimestamp();
    void setTimestamp(long timestamp);
	
    long getTimeToLive();
    void setTimeToLive(long timeToLive);

    Map<String, Object> getHeaders();
    void setHeaders(Map<String, Object> headers);
	Object getHeader(String name);
    void setHeader(String name, Object value);
	boolean headerExists(String name);
    
    boolean isExpired();
    boolean isExpired(long millis);
    long getRemainingTimeToLive();
    long getRemainingTimeToLive(long millis);
    
    public Message copy();
    public Message clone() throws CloneNotSupportedException;
}
