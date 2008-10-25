package org.jboss.webbeans.test.mock;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.model.bean.BeanModel;

/**
 * An implementation used for unit testing only.
 * @author David Allen
 *
 */
public class MockObserverImpl<T> extends ObserverImpl<T> {

	private Object specializedInstance;
	
	

	public MockObserverImpl(BeanModel<?, ?> componentModel,
         InjectableMethod<?> observer, Class<T> eventType)
   {
      super(componentModel, observer, eventType);
   }

   @Override
	protected final Object getInstance(Manager manager) {
		return specializedInstance;
	}

	/**
	 * The most specialized instance of this observer type.
	 * @param instance The instance to use for testing
	 */
	public final void setInstance(Object instance)
	{
		specializedInstance = instance;
	}

}
