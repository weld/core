package org.jboss.weld.bean.builtin;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CALL_PROXIED_METHOD;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javassist.util.proxy.MethodHandler;

import org.slf4j.cal10n.LocLogger;

public class CallableMethodHandler implements MethodHandler, Serializable
{
   
   private static final long serialVersionUID = -1348302663981663427L;
   private static final LocLogger log = loggerFactory().getLogger(BEAN);

   private final Callable<?> callable;
   
   public CallableMethodHandler(Callable<?> callable)
   {
      super();
      this.callable = callable;
   }

   public Object invoke(Object self, Method proxiedMethod, Method proceed, Object[] args) throws Throwable
   {
      // Ignore calls to finalize
      if ("finalize".equals(proxiedMethod.getName()))
      {
         return null;
      }
      Object instance = callable.call();
      if (instance == null)
      {
         throw new NullPointerException("Unable to obtain instance. Bean: " + callable);
      }
      try
      {
         Object returnValue = proxiedMethod.invoke(instance, args);
         log.trace(CALL_PROXIED_METHOD, proxiedMethod, instance, args, returnValue == null ? null : returnValue);
         return returnValue;
      }
      catch (InvocationTargetException e) 
      {
         // Unwrap the ITE
         if (e.getCause() != null)
         {
            throw e.getCause();
         }
         else
         {
            throw e;
         }
      }
   }

}
