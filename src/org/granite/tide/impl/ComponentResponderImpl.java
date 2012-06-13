package org.granite.tide.impl;

import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.TideResponder;
import org.granite.tide.server.ComponentResponder;


/**
 * @author William DRAI
 */
public class ComponentResponderImpl implements ComponentResponder {
    
    private Context sourceContext;
    private Component component;
    private String componentName;
    private String operation;
    private Object[] args;
    private Handler handler;
    private TideResponder<?> tideResponder;
    private Object info;
    
    
    public ComponentResponderImpl(Context sourceContext, Handler handler, Component component, String operation, Object[] args, Object info, TideResponder<?> tideResponder) {        
        this.sourceContext = sourceContext;
        this.handler = handler;
        this.component = component;
        this.componentName = component != null ? component.getName() : null;
        this.operation = operation;
        this.args = args;
        this.tideResponder = tideResponder;
        this.info = info;   
    }
    
    public String getOperation() {
        return operation;
    }
    
    public Object[] getArgs() {
        return args;
    }
    public void setArgs(Object[] args) {
        this.args = args;
    }
    
    public Context getSourceContext() {
        return sourceContext;
    }
    
    public Component getComponent() {
        return component;
    }
    
    public void result(ResultEvent event) {
        handler.result(sourceContext, event, info, componentName, operation, tideResponder, this);
        
    }
    
    public void fault(FaultEvent event) {
        handler.fault(sourceContext, event, info, componentName, operation, tideResponder, this);
    }    
}