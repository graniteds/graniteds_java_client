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

package org.granite.client.messaging.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.client.messaging.codec.MessagingCodec;

/**
 * @author Franck WOLFF
 */
public class DefaultTransportMessage<M> implements TransportMessage {

	private final String id;
	private final boolean connect;
	private final String clientId;
	private final String sessionId;
	private final M message;
	private final MessagingCodec<M> codec;

	public DefaultTransportMessage(String id, boolean connect, String clientId, String sessionId, M message, MessagingCodec<M> codec) {
		this.id = id;
		this.connect = connect;
		this.clientId = clientId;
		this.sessionId = sessionId;
		this.message = message;
		this.codec = codec;
	}

	public String getId() {
		return id;
	}
	
	public boolean isConnect() {
		return connect;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public String getContentType() {
		return codec.getContentType();
	}

	@Override
	public void encode(OutputStream os) throws IOException {
		codec.encode(message, os);
	}
}
