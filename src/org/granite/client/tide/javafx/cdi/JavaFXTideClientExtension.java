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

package org.granite.client.tide.javafx.cdi;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Inject;

import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.Application;
import org.granite.client.tide.ApplicationConfigurable;
import org.granite.client.tide.cdi.CDIContextManager;
import org.granite.client.tide.cdi.CDIEventBus;
import org.granite.client.tide.cdi.ViewContext;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.javafx.JavaFXDataManager;
import org.granite.client.tide.javafx.JavaFXApplication;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class JavaFXTideClientExtension implements Extension {
	
	private static final Logger log = Logger.getLogger(JavaFXTideClientExtension.class);
	
	private Application application = new JavaFXApplication();
	
	
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
		log.debug("Register internal Tide beans");
		event.addAnnotatedType(beanManager.createAnnotatedType(JavaFXApplication.class));
		event.addAnnotatedType(beanManager.createAnnotatedType(CDIEventBus.class));
		event.addAnnotatedType(beanManager.createAnnotatedType(JavaFXCDIContextManager.class));
	}
	
	public void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
		log.debug("Register internal Tide scopes");
		event.addContext(new ViewContext());
	}
	
	public void processProducer(@Observes ProcessProducer<Object, Object> event, BeanManager beanManager) {
		event.setProducer(new ProducerWrapper<Object>(application, beanManager, event.getProducer(), event.getAnnotatedMember()));
	}
	
	public void processInjectionTarget(@Observes ProcessInjectionTarget<Object> event, BeanManager beanManager) {
		event.setInjectionTarget(new InjectionTargetWrapper<Object>(application, beanManager, event.getInjectionTarget(), event.getAnnotatedType()));
	}
	
	
	@ApplicationScoped
	public static class JavaFXCDIContextManager extends CDIContextManager {
		
		protected JavaFXCDIContextManager() {
			super();
			// CDI proxying...
		}
		
		@Inject
		public JavaFXCDIContextManager(Application platform, EventBus eventBus) {
			super(platform, eventBus);
		}
		
		@Produces
		public Context getContext() {
			return getContext(null);
		}
		
		@Produces
		public EntityManager getEntityManager() {
			return getContext(null).getEntityManager();
		}
		
		@Produces
		public JavaFXDataManager getDataManager() {
			return (JavaFXDataManager)super.getDataManager();
		}
	}
	
	public static class ProducerWrapper<T> implements Producer<T> {
		
		private Application application;
		private BeanManager beanManager;
		private Producer<T> producer;
		private AnnotatedMember<T> annotatedMember;
		
		public ProducerWrapper(Application application, BeanManager beanManager, Producer<T> producer, AnnotatedMember<T> annotatedMember) {
			this.application = application;
			this.beanManager = beanManager;
			this.producer = producer;
			this.annotatedMember = annotatedMember;
		}
		
		@Override
		public void dispose(T instance) {
			producer.dispose(instance);
		}

		@Override
		public Set<InjectionPoint> getInjectionPoints() {
			return producer.getInjectionPoints();
		}

		@Override
		public T produce(CreationalContext<T> cc) {
			T instance = producer.produce(cc);
			
			if (instance instanceof NameAware) {
				Set<Bean<?>> beans = beanManager.getBeans(annotatedMember.getBaseType());
				if (beans.size() == 1)
					((NameAware)instance).setName(beans.iterator().next().getName());
			}
			
			if (instance instanceof ContextAware) {
				Set<Bean<?>> beans = beanManager.getBeans(Context.class);
				if (beans.size() == 1) {
					Bean<?> bean = beans.iterator().next();
					CreationalContext<?> ccc = beanManager.createCreationalContext(bean);
					((ContextAware)instance).setContext((Context)beanManager.getReference(bean, Context.class, ccc));
				}
			}
			
			if (instance != null && instance.getClass().isAnnotationPresent(ApplicationConfigurable.class))
				application.configure(instance);
			
			return instance;
		}
	}
	
	public static class InjectionTargetWrapper<T> implements InjectionTarget<T> {
		
		private Application platform;
		private BeanManager beanManager;
		private InjectionTarget<T> injectionTarget;
		private AnnotatedType<T> annotatedType;
		
		public InjectionTargetWrapper(Application platform, BeanManager beanManager, InjectionTarget<T> injectionTarget, AnnotatedType<T> annotatedType) {
			this.platform = platform;
			this.beanManager = beanManager;
			this.injectionTarget = injectionTarget;
			this.annotatedType = annotatedType;
		}

		@Override
		public void dispose(T instance) {
			injectionTarget.dispose(instance);
		}
		
		@Override
		public Set<InjectionPoint> getInjectionPoints() {
			return injectionTarget.getInjectionPoints();
		}

		@Override
		public void inject(T instance, CreationalContext<T> cc) {
			injectionTarget.inject(instance, cc);
		}

		@Override
		public T produce(CreationalContext<T> cc) {
			return injectionTarget.produce(cc);
		}

		@Override
		public void postConstruct(T instance) {
			if (instance instanceof NameAware) {
				Set<Bean<?>> beans = beanManager.getBeans(annotatedType.getBaseType());
				if (beans.size() == 1)
					((NameAware)instance).setName(beans.iterator().next().getName());
			}
			
			if (instance instanceof ContextAware) {
				Set<Bean<?>> beans = beanManager.getBeans(Context.class);
				if (beans.size() == 1) {
					Bean<?> bean = beans.iterator().next();
					CreationalContext<?> ccc = beanManager.createCreationalContext(bean);
					((ContextAware)instance).setContext((Context)beanManager.getReference(bean, Context.class, ccc));
				}
			}
			
			if (instance != null && instance.getClass().isAnnotationPresent(ApplicationConfigurable.class))
				platform.configure(instance);
			
			injectionTarget.postConstruct(instance);
		}

		@Override
		public void preDestroy(T instance) {
			injectionTarget.preDestroy(instance);
		}
	}
}
