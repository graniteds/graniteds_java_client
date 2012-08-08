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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.server.ArgumentPreprocessor;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.InvocationInterceptor;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TrackingContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;
import org.granite.tide.invocation.InvocationCall;

/**
 * @author William DRAI
 */
public class ComponentImpl implements Component, ContextAware, NameAware, InvocationHandler {
    
	private static final Logger log = Logger.getLogger(ComponentImpl.class);


    private String name;
    private Context context;
    private final ServerSession serverSession;
    
    
    public ComponentImpl(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
    
    
    public void setName(String name) {
    	this.name = name;
    }
    public String getName() {
    	return name;
    }
    
    public void setContext(Context context) {
    	this.context = context;
    }    
    protected Context getContext() {
    	return context;
    }
    
    protected ServerSession getServerSession() {
    	return serverSession;
    }
    
    
    @SuppressWarnings("unchecked")
    public <T> Future<T> call(String operation, Object... args) {
        Context context = this.context;
        
        if (args != null && args.length > 0 && args[0] instanceof Context) {
            context = (Context)args[0];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 1; i < args.length-1; i++)
            	newArgs[i-1] = args[i];
            args = newArgs;
        }
        
        return (Future<T>)callComponent(context, operation, args, false);
    }

    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (!method.getDeclaringClass().isAnnotationPresent(RemoteClass.class))
			return method.invoke(proxy, args);
		
		return callComponent(getContext(), method.getName(), args, false);
	}


    /**
     *  Calls a remote component
     * 
     *  @param component the target component
     *  @param op name of the called metho
     *  @param arg method parameters
     *  @param withContext add context sync data to call
     * 
     *  @return the operation token
     */
    @SuppressWarnings("unchecked")
	protected <T> Future<T> callComponent(Context context, String operation, Object[] args, boolean withContext) {
    	context.checkValid();
        
        log.debug("callComponent %s.%s", getName(), operation);
        
        TideResponder<?> responder = null;
        if (args != null && args.length > 0 && args[args.length-1] instanceof TideResponder) {
            responder = (TideResponder<?>)args[args.length-1];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 0; i < args.length-1; i++)
            	newArgs[i] = args[i];
            args = newArgs;
        }
        
		// Force generation of uids by merging all arguments in the current context
        context.getEntityManager().initMerge();
        List<Object> argsList = Arrays.asList(args);
		for (int i = 0; i < args.length; i++) {
			if (argsList.get(i) instanceof PropertyHolder)
				argsList.set(i, ((PropertyHolder)args[i]).getObject());
		}
		argsList = (List<Object>)context.getEntityManager().mergeExternalData(argsList);
		for (int i = 0; i < args.length; i++)
			args[i] = argsList.get(i);
		
        Method method = null;
        // TODO: improve method matching
        for (Method m : getClass().getMethods()) {
            if (m.getName().equals(operation) && m.getParameterTypes().length == args.length) {
                method = m;
                break;
            }
        }
        if (method != null) {
            // Call argument preprocessors if necessary before sending arguments to server
            ArgumentPreprocessor[] apps = context.allByType(ArgumentPreprocessor.class);
            if (apps != null) {
                for (ArgumentPreprocessor app : apps)
                    args = app.preprocess(method, args);
            }
        }
        
        TrackingContext trackingContext = serverSession.getTrackingContext();
        Future<T> future = null;
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            ResponseMessageFuture rmf = invoke(context, this, operation, args, responder, withContext, null);
            future = context.getBeanManager().buildFutureResult(rmf);
        }
        finally {
            trackingContext.setEnabled(saveTracking);
        }
        
        if (withContext)
            trackingContext.clearUpdates(true);
        
        // TODO: conversation contexts
		serverSession.trackCall();
//        if (remoteConversation != null)
//            remoteConversation.call();
        
        return future;
    }
    
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseMessageFuture invoke(Context context, Component component, String operation, Object[] args, TideResponder<?> tideResponder, 
                           boolean withContext, ComponentListener.Handler handler) {
        log.debug("invokeComponent %s > %s.%s", context.getContextId(), component.getName() != null ? component.getName() : component.getClass().getName(), operation);
        
        ComponentListener.Handler h = handler != null ? handler : new ComponentListener.Handler() {            
			@Override
            public void result(Context context, ResultEvent event, Object info, String componentName,
                    String operation, TideResponder<?> tideResponder, ComponentListener componentResponder) {
            	context.callLater(new ResultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentResponder));
            }
            
            @Override
            public void fault(Context context, FaultEvent event, Object info, String componentName,
                    String operation, TideResponder<?> tideResponder, ComponentListener componentResponder) {
            	context.callLater(new FaultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentResponder));
            }
        };
        ComponentListener componentListener = new ComponentListenerImpl(context, h, component, operation, args, null, tideResponder);
        
        InvocationInterceptor[] interceptors = context.allByType(InvocationInterceptor.class);
        if (interceptors != null) {
            for (InvocationInterceptor interceptor : interceptors)
                interceptor.beforeInvocation(context, component, operation, args, componentListener);
        }
        
        context.getContextManager().destroyFinishedContexts();
        
//        // Force generation of uids by merging all arguments in the current context
//        for (int i = 0; i < args.length; i++) {
//            if (args[i] instanceof PropertyHolder)
//                args[i] = ((PropertyHolder)args[i]).getObject();
//            args[i] = entityManager.mergeExternal(args[i], null);
//        }
//        
//        // Call argument preprocessors before sending arguments to server
//        var method:Method = Type.forInstance(component).getInstanceMethodNoCache(op);
//        for each (var app:IArgumentPreprocessor in allByType(IArgumentPreprocessor, true))
//            componentResponder.args = app.preprocess(method, args);
        
    	Object[] call = new Object[5];
    	call[0] = componentListener.getComponent().getName();
    	String componentClassName = null;
    	if (componentListener.getComponent().getClass() != ComponentImpl.class) {
    		RemoteClass remoteClass = componentListener.getComponent().getClass().getAnnotation(RemoteClass.class);
    		componentClassName = remoteClass != null ? remoteClass.value() : componentListener.getComponent().getClass().getName();
    	}
    	call[1] = componentClassName;
    	call[2] = componentListener.getOperation();
    	call[3] = componentListener.getArgs();
    	call[4] = new InvocationCall();
    	
        RemoteService ro = serverSession.getRemoteService();
        ResponseMessageFuture rmf = ro.newInvocation("invokeComponent", call).addListener(componentListener).invoke();
        
        serverSession.checkWaitForLogout();
        
        return rmf;
    }
}
