package org.jboss.webbeans.event;

import java.lang.reflect.Method;
import java.util.List;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.Parameter;

/**
 * A specialized injectable method where one of the injected parameters is an
 * event object.
 * 
 * @author David Allen
 *
 */
public class ObserverMethod extends InjectableMethod
{

   public ObserverMethod(Method method)
   {
      super(method);
   }

   /**
    * Invokes the method on the given component instance and uses the specified
    * event object for parameter injection.
    * 
    * @param manager The WebBeans manager
    * @param instance The component instance to invoke the observer method on
    * @param event The event object being fired
    */
   public void invoke(Manager manager, Object instance, Object event)
   {
      try
      {
         getMethod().invoke(instance, getParameterValues(manager, event));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Unable to invoke " + getMethod() + " on " + instance, e);
      }
   }

   /**
    * Creates a list of parameter values to inject and uses the specified event object
    * to inject the observed event.
    * 
    * @param manager The WebBeans manager
    * @param event The event being fired
    * @return an array of objects that serve as arguments for the invocation of the method
    */
   @SuppressWarnings("unchecked")
   public Object[] getParameterValues(Manager manager, Object event)
   {
      // Let the super class get the parameter values, but substitute the event
      // object so that we know for certain it is the correct one.
      Object[] parameterValues = super.getParameterValues(manager);
      List<Parameter> parms = this.getParameters();
      int i = 0;
      for (Parameter p : parms)
      {
         if (p.getType().isAssignableFrom(event.getClass()))
         {
            parameterValues[i] = event;
            break;
         }
         i++;
      }
      return parameterValues;
   }

}
