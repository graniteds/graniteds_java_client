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

package org.granite.client.messaging.transport.apache;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.AbstractTransport;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportHttpStatusException;
import org.granite.client.messaging.transport.TransportIOException;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;

/**
 * @author Franck WOLFF
 */
public class ApacheAsyncTransport extends AbstractTransport implements HTTPTransport {
	
	private static final Logger log = Logger.getLogger(ApacheAsyncTransport.class);

	protected CloseableHttpAsyncClient httpClient = null;
	protected CookieStore cookieStore = new BasicCookieStore();
	protected RequestConfig defaultRequestConfig = null;
	
	public ApacheAsyncTransport() {
		this(null);
	}
	
	public ApacheAsyncTransport(RequestConfig defaultRequestConfig) {
		this.defaultRequestConfig = defaultRequestConfig;
	}

	public void configure(HttpAsyncClientBuilder clientBuilder) {
		// Can be overwritten...
	}

	@Override
	public synchronized boolean start() {
		if (isStarted())
			return true;
		
		stop();
		
		log.info("Starting Apache HttpAsyncClient transport...");
		
		try {
			RequestConfig requestConfig = defaultRequestConfig;
			if (requestConfig == null)
				requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
			
			HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom();
			httpClientBuilder.setDefaultCookieStore(cookieStore);
			httpClientBuilder.setDefaultRequestConfig(requestConfig);
			configure(httpClientBuilder);
			httpClient = httpClientBuilder.build();
			
			httpClient.start();
			
			log.info("Apache HttpAsyncClient transport started.");
			return true;
		}
		catch (Exception e) {
			httpClient = null;
			getStatusHandler().handleException(new TransportException("Could not start Apache HttpAsyncClient", e));

			log.error(e, "Apache HttpAsyncClient failed to start.");
			return false;
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return httpClient != null;
	}

	@Override
	public TransportFuture send(final Channel channel, final TransportMessage message) throws TransportException {
		synchronized (this) {
		    if (httpClient == null) {
		    	TransportIOException e = new TransportIOException(message, "Apache HttpAsyncClient not started");
		    	getStatusHandler().handleException(e);
		    	throw e;
			}
		}
	    
		if (!message.isConnect())
			getStatusHandler().handleIO(true);
		
		try {
		    HttpPost request = new HttpPost(channel.getUri());
			request.setHeader("Content-Type", message.getContentType());
			request.setHeader("GDSClientType", "java");	// Notify the server that we expect Java serialization mode
			
			PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(512);
			try {
				message.encode(os);
			}
			catch (IOException e) {
				throw new TransportException("Message serialization failed: " + message.getId(), e);
			}
			request.setEntity(new ByteArrayEntity(os.getBytes(), 0, os.size()));
			
			final Future<HttpResponse> future = httpClient.execute(request, new FutureCallback<HttpResponse>() {
	
	            public void completed(HttpResponse response) {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	            	if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	            		channel.onError(message, new TransportHttpStatusException(
	            			response.getStatusLine().getStatusCode(),
	            			response.getStatusLine().getReasonPhrase())
	            		);
	            		return;
	            	}
	            	
	        		InputStream is = null;
	        		try {
	        			is = response.getEntity().getContent();
	        			channel.onMessage(is);
	        		}
	        		catch (Exception e) {
		            	getStatusHandler().handleException(new TransportIOException(message, "Could not deserialize message", e));
					}
	        		finally {
	        			if (is != null) try {
	        				is.close();
	        			}
	        			catch (Exception e) {
	        			}
	        		}
	            }
	
	            public void failed(Exception e) {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	
	            	channel.onError(message, e);
	            	getStatusHandler().handleException(new TransportIOException(message, "Request failed", e));
	            }
	
	            public void cancelled() {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	            	channel.onCancelled(message);
	            }
	        });
			
			return new TransportFuture() {
				@Override
				public boolean cancel() {
					boolean cancelled = false;
					try {
						cancelled = future.cancel(true);
					}
					catch (Exception e) {
						log.error(e, "Cancel request failed");
					}
					return cancelled;
				}
			};
		}
		catch (Exception e) {
        	if (!message.isConnect())
        		getStatusHandler().handleIO(false);
			
			TransportIOException f = new TransportIOException(message, "Request failed", e);
        	getStatusHandler().handleException(f);
			throw f;
		}
	}
	
	public synchronized void poll(final Channel channel, final TransportMessage message) throws TransportException {
		throw new TransportException("Not implemented");
	}

	@Override
	public synchronized void stop() {
		if (httpClient == null)
			return;
		
		log.info("Stopping Apache HttpAsyncClient transport...");

		super.stop();
		
		try {
			httpClient.close();
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not stop Apache HttpAsyncClient", e));

			log.error(e, "Apache HttpAsyncClient failed to stop properly.");
		}
		finally {
			httpClient = null;
		}
		
		log.info("Apache HttpAsyncClient transport stopped.");
	}
}
