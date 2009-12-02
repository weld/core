package org.jboss.weld.bean.builtin;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CALL_PROXIED_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.NULL_INSTANCE;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;

import javassist.util.proxy.MethodHandler;

import org.jboss.weld.Container;
import org.jboss.weld.NullInstanceException;
import org.jboss.weld.bootstrap.api.Service;
import org.slf4j.cal10n.LocLogger;

public class CallableMethodHandler implements MethodHandler, Serializable
{
   
   public static class CallableMethodHandlerCleaner implements Service
   {
      
      private final Collection<CallableMethodHandler> callableMethodHandlers;
      
      public CallableMethodHandlerCleaner()
      {
         this.callableMethodHandlers = new HashSet<CallableMethodHandler>();
      }
      
      private void add(CallableMethodHandler callableMethodHandler)
      {
         this.callableMethodHandlers.add(callableMethodHandler);
      }
      
      public void cleanup()
      {
         for (CallableMethodHandler callableMethodHandler : callableMethodHandlers)
         {
            callableMethodHandler.cleanup();
         }
         callableMethodHandlers.clear();
      }
      
   }
   
   private static final long serialVersionUID = -1348302663981663427L;
   private static final LocLogger log = loggerFactory().getLogger(BEAN);

   // Can't make this final, need to deallocate on shutdown
   private Callable<?> callable;
   
   public CallableMethodHandler(Callable<?> callable)
   {
      super();
      this.callable = callable;
      Container.instance().deploymentServices().get(CallableMethodHandlerCleaner.class).add(this);
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
         throw new NullInstanceException(NULL_INSTANCE, callable);
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
   
   private void cleanup()
   {
      this.callable = null;
   }

}
