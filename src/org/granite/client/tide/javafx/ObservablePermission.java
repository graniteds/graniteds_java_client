package org.granite.client.tide.javafx;

import javafx.beans.property.ReadOnlyBooleanPropertyBase;

import org.granite.client.tide.Context;
import org.granite.client.tide.javafx.spring.Identity;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.SimpleTideResponder;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;

public class ObservablePermission extends ReadOnlyBooleanPropertyBase {
	
	private final BaseIdentity identity;
	private final Context context;
	private final ServerSession serverSession;
	private final String name;
	private final Object entity;
	private final String action;
	
	private Boolean hasPermission = null;
	
	
	public ObservablePermission(Identity identity, Context context, ServerSession serverSession, String name, Object entity, String action) {
		super();
		this.identity = identity;
		this.context = context;
		this.serverSession = serverSession;
		this.name = name;
		this.entity = entity;
		this.action = action;
	}

	@Override
	public Object getBean() {
		return identity;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean get() {
		if (hasPermission == null) {
			if (this.identity.isLoggedIn())
				getFromRemote(null);
			return false;
    	}
		return hasPermission;
    }
	
	public boolean get(TideResponder<Boolean> tideResponder) {
		if (hasPermission != null) {
	    	if (tideResponder != null) {
	    		TideResultEvent<Boolean> event = new TideResultEvent<Boolean>(context, serverSession, null, hasPermission);
	    		tideResponder.result(event);
	    	}
	    	return hasPermission;
		}
		if (this.identity.isLoggedIn())
			getFromRemote(tideResponder);
		return false;
	}    
	
	public void getFromRemote(final TideResponder<Boolean> tideResponder) {
		this.identity.call(name, entity, action, new SimpleTideResponder<Boolean>() {
			@Override
			public void result(TideResultEvent<Boolean> event) {
				if (tideResponder != null)
					tideResponder.result(event);
				hasPermission = event.getResult();
				fireValueChangedEvent();
			}
			
			@Override
			public void fault(TideFaultEvent event) {
				if (tideResponder != null)
					tideResponder.fault(event);
				clear();
			}
		});
	}
	
	public void clear() {
		hasPermission = null;
		fireValueChangedEvent();
	}
}