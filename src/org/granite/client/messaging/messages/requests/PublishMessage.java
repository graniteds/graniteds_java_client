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

import org.granite.client.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public final class PublishMessage extends AbstractTopicRequestMessage {

	private Object body = null;

	public PublishMessage() {
	}

	public PublishMessage(String destination, String topic, Object body) {
		this(null, destination, topic, body);
	}

	public PublishMessage(String clientId, String destination, String topic, Object body) {
		super(clientId, destination, topic);
		
		this.body = body;
	}

	public PublishMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination,
		String topic,
		Object body) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);
		
		this.body = body;
	}

	@Override
	public Type getType() {
		return Type.PUBLISH;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public Message copy() {
		PublishMessage message = new PublishMessage();
		
		copy(message);
		
		return message;
	}
}
