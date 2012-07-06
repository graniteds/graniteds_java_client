/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

package org.granite.tide.client.test;

import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportStatusHandler.LogEngineStatusHandler;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;

/**
 * @author Franck WOLFF
 */
public class CallGranitedsEjb3 {

	public static void main(String[] args) throws Exception {		
		
		URI uri = new URI("http://localhost:8080/graniteds-ejb3/graniteamf/amf");
		
		System.out.println("Connecting to: " + uri);

		// Create and configure a transport.
		HTTPTransport transport = new ApacheAsyncTransport();
		transport.setStatusHandler(new LogEngineStatusHandler() {
			
			@Override
			public void handleIO(boolean active) {
				//super.handleIO(active);
			}

			@Override
			public void handleException(TransportException e) {
				//super.handleException(e);
				//sem.release();
			}
		});
		transport.start();
		
		// Create a channel with the specified uri.
		AMFRemotingChannel channel = new AMFRemotingChannel(transport, "my-graniteamf", uri, 2);

		// Login (credentials will be sent with the first call).
		channel.setCredentials(new UsernamePasswordCredentials("admin", "admin"));

		// Create a remote object with the channel and a destination.
		RemoteService ro = new RemoteService(channel, "person");

		final Semaphore sem = new Semaphore(0);
		
		ResponseListener listener = new ResultFaultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				StringBuilder sb = new StringBuilder("onResult {");
				for (ResponseMessage response : event.getResponse())
					sb.append("\n    response=").append(response.toString().replace("\n", "\n    "));
				sb.append("\n}");
				System.out.println(sb);
				
				sem.release();
			}

			@Override
			public void onFault(FaultEvent event) {
				StringBuilder sb = new StringBuilder("onFault {");
				for (ResponseMessage response : event.getResponse())
					sb.append("\n    response=").append(response.toString().replace("\n", "\n    "));
				sb.append("\n}");
				System.out.println(sb);

				sem.release();
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				System.out.println(event);
				sem.release();
			}
		};
		
		ro.newInvocation("findAllPersons").addListener(listener).appendInvocation("findAllCountries").invoke();
		ro.newInvocation("findAllPersons").addListener(listener).setTimeToLive(5, TimeUnit.MINUTES).invoke();
		ro.newInvocation("findAllPersons").addListener(listener).invoke();
		ro.newInvocation("findAllPersons").addListener(listener).invoke();
		ro.newInvocation("findAllPersons").setTimeToLive(10, TimeUnit.MILLISECONDS).addListener(listener).invoke();

		sem.acquire(5);
	
		// Stop transport (must be done!)
		transport.stop();
		
		System.out.println("Done.");
	}
}
