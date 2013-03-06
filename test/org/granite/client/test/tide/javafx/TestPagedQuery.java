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

package org.granite.client.test.tide.javafx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.messages.Message;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.test.MockRemoteService;
import org.granite.client.test.ResponseBuilder;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.test.tide.MockServiceFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.collections.javafx.PageChangeListener;
import org.granite.client.tide.collections.javafx.PagedCollection;
import org.granite.client.tide.collections.javafx.PagedQuery;
import org.granite.client.tide.impl.DefaultPlatform;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideRpcEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestPagedQuery {
    
    private ContextManager contextManager;
    private Context ctx;
    private ServerSession serverSession;
    
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new DefaultPlatform());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext();
        serverSession = new ServerSession("test", "/test", "localhost", 8080);
        serverSession.setServiceFactory(new MockServiceFactory());
        ctx.set(serverSession);
        serverSession.start();
    }
    
    @After
    public void tearDown() throws Exception {
    	serverSession.stop();
    }

    @Test
    public void testPagedQuery() throws Exception {        
        PagedQuery<Person, Person> personList = new PagedQuery<Person, Person>(serverSession);
        ctx.set("personList", personList);
        
        MockRemoteService.setResponseBuilder(new ResponseBuilder() {
            @Override
            public Message buildResponseMessage(RemoteService service, RequestMessage request) {
            	InvocationMessage invocation = (InvocationMessage)request;
            	
                if (!invocation.getParameters()[0].equals("personList"))
                    return new FaultMessage();
                
                String method = (String)invocation.getParameters()[2];
                // Object[] args = ((Object[])invocation.getParameters()[3]);
                
                if (method.equals("find")) {
                    List<Person> list = new ArrayList<Person>();
                    list.add(new Person());
                    list.add(new Person());
                    java.util.Map<String, Object> result = new HashMap<String, Object>();
                    result.put("firstResult", 0);
                    result.put("maxResults", 12);
                    result.put("resultCount", 2);
                    result.put("resultList", list);
                    return new ResultMessage(null, null, result);
                }
                return null;
            }
        });
        
        final Semaphore sem = new Semaphore(1);
        sem.acquire();
        
        personList.addListener(new PageChangeListener<Person>() {
			@Override
			public void pageChanged(PagedCollection<Person> collection, TideRpcEvent event) {
				sem.release();
			}        	
        });
        
        personList.get(3);	// Trigger loading
        
        if (!sem.tryAcquire(500000, TimeUnit.MILLISECONDS)) {
        	Assert.fail("Timeout on find");
        	return;
        }
        
        Assert.assertEquals("Persons count", 2, personList.size());
        Assert.assertNull("Person element", personList.get(3));
    }
}
