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

package org.granite.client.tide.impl;

import java.util.Map;

import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.Event.Type;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.Fault;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.logging.Logger;

/**
 *  @private
 *  Implementation of fault handler
 *  
 *  @param sourceContext source context of remote call
 *  @param info fault object
 *  @param componentName component name
 *  @param op remote operation
 *  @param tideResponder Tide responder for the remote call
 *  
 * @author William DRAI
 */
public class FaultHandler<T> implements Runnable {
	
	private static final Logger log = Logger.getLogger(FaultHandler.class);
	
	private final ServerSession serverSession;
	private final Context sourceContext;
	private final String componentName;
	private final String operation;
	private final Event event;
	@SuppressWarnings("unused")
	private final Object info;
	private final TideResponder<T> tideResponder;
	private final ComponentListener<T> componentResponder;
	private boolean executed = false;
	
	
	public FaultHandler(ServerSession serverSession, Context sourceContext, String componentName, String operation, Event event, Object info, 
			TideResponder<T> tideResponder, ComponentListener<T> componentResponder) {
		this.serverSession = serverSession;
		this.sourceContext = sourceContext;
		this.componentName = componentName;
		this.operation = operation;
		this.event = event;
		this.info = info;
		this.tideResponder = tideResponder;
		this.componentResponder = componentResponder;
	}

	public void run() {
		if (executed)
			return;
		executed = true;
		
        log.error("fault %s", event.toString());
       
        // TODO: conversation contexts
//        var sessionId:String = faultEvent.message.headers[Tide.SESSION_ID_TAG];
//        var conversationId:String = null;
//        if (faultEvent.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG])
//            conversationId = faultEvent.message.headers[Tide.CONVERSATION_TAG];
//        var wasConversationCreated:Boolean = faultEvent.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
//        var wasConversationEnded:Boolean = faultEvent.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
//        
//        var context:Context = _contextManager.retrieveContext(sourceContext, conversationId, wasConversationCreated, wasConversationEnded);
        
        Context context = sourceContext.getContextManager().retrieveContext(sourceContext, null, false, false);

        FaultMessage emsg = null;
        Map<String, Object> extendedData = null;
        if (event instanceof FaultEvent) {
	       	emsg = ((FaultEvent)event).getMessage();
	       	FaultMessage m = emsg;
	       	extendedData = emsg != null ? emsg.getExtended() : null;
	        do {
	            if (m != null && m.getCode() != null && m.isSecurityFault()) {
	                emsg = m;
	                extendedData = emsg != null ? emsg.getExtended() : null;
	                break;
	            }
	            // TODO: check WTF we should do here
	            if (m != null && m.getCause() instanceof FaultEvent)
	                m = (FaultMessage)((FaultEvent)m.getCause()).getCause();
	            else if (m.getCause() instanceof FaultMessage)
	                m = (FaultMessage)m.getCause();
	            else
	            	m = null;
	        }
	        while (m != null);
	        
	        serverSession.handleFaultEvent((FaultEvent)event, emsg);
        }
        else
	        serverSession.handleIssueEvent((IssueEvent)event);
        
        serverSession.handleFault(context, componentName, operation, emsg);
        
        boolean handled = false;
        Fault fault = null;
        if (event instanceof FaultEvent) {
        	fault = new Fault(emsg.getCode(), emsg.getDescription(), emsg.getDetails());
	        fault.setContent(((FaultEvent)event).getMessage());
	        fault.setCause(((FaultEvent)event).getCause());
        }
        else if (event.getType() == Type.FAILURE) {
        	fault = new Fault(Code.CLIENT_CALL_FAILED, null, ((FailureEvent)event).getCause() != null ? ((FailureEvent)event).getCause().getMessage() : null);
        	fault.setCause(((FailureEvent)event).getCause());
        	emsg = new FaultMessage(null, null, Code.CLIENT_CALL_FAILED, null, null, null, null);
        }
        else if (event.getType() == Type.TIMEOUT) {
        	fault = new Fault(Code.CLIENT_CALL_TIMED_OUT, null, String.valueOf(((TimeoutEvent)event).getTime()));
        	emsg = new FaultMessage(null, null, Code.CLIENT_CALL_TIMED_OUT, null, null, null, null);
        }
        else if (event.getType() == Type.CANCELLED) {
        	fault = new Fault(Code.CLIENT_CALL_CANCELLED, null, null);
        	emsg = new FaultMessage(null, null, Code.CLIENT_CALL_TIMED_OUT, null, null, null, null);
        }
        
        TideFaultEvent faultEvent = new TideFaultEvent(context, serverSession, componentResponder, fault, extendedData);
        if (tideResponder != null) {
            tideResponder.fault(faultEvent);
            if (faultEvent.isDefaultPrevented())
                handled = true;
        }
        
        if (!handled) {
            ExceptionHandler[] exceptionHandlers = context.getContextManager().getContext(null).allByType(ExceptionHandler.class);
            if (exceptionHandlers != null && emsg != null) {
                // Lookup for a suitable exception handler
                for (ExceptionHandler handler : exceptionHandlers) {
                    if (handler.accepts(emsg)) {
                        handler.handle(context, emsg, faultEvent);
                        handled = true;
                        break;
                    }
                }
                if (!handled)
                    log.error("Unhandled fault: " + emsg.getCode() + ": " + emsg.getDescription());
            }
            else if (exceptionHandlers != null && exceptionHandlers.length > 0 && event instanceof FaultEvent && ((FaultEvent)event).getMessage() instanceof FaultMessage) {
                // Handle fault with default exception handler
                exceptionHandlers[0].handle(context, (FaultMessage)((FaultEvent)event).getMessage(), faultEvent);
            }
            else {
                log.error("Unknown fault: " + event.toString());
            }
        }
        
        if (!handled && !serverSession.isLogoutInProgress())
        	context.getEventBus().raiseEvent(context, ServerSession.CONTEXT_FAULT, event instanceof FaultEvent ? ((FaultEvent)event).getMessage() : null);
        
        serverSession.tryLogout();
    }
}