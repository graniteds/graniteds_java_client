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

package org.granite.client.messaging.channel.amf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.configuration.DefaultConfiguration;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.codec.AMF0MessagingCodec;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.AbstractResponseMessage;
import org.granite.client.messaging.transport.DefaultTransportMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AMFRemotingChannel extends AbstractAMFChannel implements RemotingChannel {
	
	private final AMF0MessagingCodec codec;
	private volatile int index = 1;
	
	public AMFRemotingChannel(Transport transport, String id, URI uri) {
		this(transport, id, uri, 5);
	}
	
	public AMFRemotingChannel(Transport transport, String id, URI uri, int maxConcurrentRequests) {
		this(transport, DefaultConfiguration.getInstance(), id, uri, maxConcurrentRequests);
	}
	
	public AMFRemotingChannel(Transport transport, Configuration configuration, String id, URI uri, int maxConcurrentRequests) {
		super(transport, id, uri, maxConcurrentRequests);
		
		this.codec = new AMF0MessagingCodec(configuration);
	}

	@Override
	protected TransportMessage createTransportMessage(AsyncToken token) throws UnsupportedEncodingException {
		AMF0Message amf0Message = new AMF0Message();
		for (Message message : convertToAmf(token.getRequest())) {
			AMF3Object data = new AMF3Object(message);
		    AMF0Body body = new AMF0Body("", "/" + (index++), new Object[]{data}, AMF0Body.DATA_TYPE_AMF3_OBJECT);
		    amf0Message.addBody(body);
		}
		return new DefaultTransportMessage<AMF0Message>(token.getId(), false, clientId, null, amf0Message, codec);
	}

	@Override
	protected ResponseMessage decodeResponse(InputStream is) throws IOException {
		final AMF0Message amf0Message = codec.decode(is);
		final int messagesCount = amf0Message.getBodyCount();
		
		if (messagesCount > 0) {
			AMF0Body body = amf0Message.getBody(0);
			
			if (!(body.getValue() instanceof AcknowledgeMessage))
				throw new RuntimeException("Message should be an AcknowledgeMessage: " + body.getValue());
			
			AcknowledgeMessage message = (AcknowledgeMessage)body.getValue();
			
			final AbstractResponseMessage response = convertFromAmf(message);
			
			AbstractResponseMessage current = response;
			for (int i = 1; i < messagesCount; i++) {
				body = amf0Message.getBody(i);
				
				if (!(body.getValue() instanceof AcknowledgeMessage))
					throw new RuntimeException("Message should be an AcknowledgeMessage: " + body.getValue());
				
				message = (AcknowledgeMessage)body.getValue();
				AbstractResponseMessage next = convertFromAmf(message);
				current.setNext(next);
				current = next;
			}
			
			return response;
		}
		
		return null;
	}
}
