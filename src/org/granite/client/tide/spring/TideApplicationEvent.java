package org.granite.client.tide.spring;

import org.granite.client.tide.Context;
import org.granite.client.tide.events.TideEvent;
import org.springframework.context.ApplicationEvent;


public class TideApplicationEvent extends ApplicationEvent implements TideEvent {

	private static final long serialVersionUID = 1L;
	
	private final String type;
	private final Object[] args;

	
	public TideApplicationEvent(Context context, String type, Object... args) {
		super(context);
		this.type = type;
		this.args = args;
	}
	
	public Context getContext() {
		return (Context)getSource();
	}
	
	public String getType() {
		return type;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
}