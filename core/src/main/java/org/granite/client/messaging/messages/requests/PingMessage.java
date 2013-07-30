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

package org.granite.client.messaging.messages.requests;

import java.util.Map;

/**
 * @author Franck WOLFF
 */
public final class PingMessage extends AbstractRequestMessage {
	
	public PingMessage() {
	}

	public PingMessage(String clientId) {
		super(clientId);
	}

	public PingMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {
		
		super(id, clientId, timestamp, timeToLive, headers);
	}

	@Override
	public Type getType() {
		return Type.PING;
	}
	
	@Override
	public PingMessage copy() {
		PingMessage message = new PingMessage();
		
		copy(message);
		
		return message;
	}
}
