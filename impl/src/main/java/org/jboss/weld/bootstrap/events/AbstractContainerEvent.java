package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

public abstract class AbstractContainerEvent
{
   
   protected static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

   private final List<Throwable> errors;
   private final BeanManagerImpl beanManager;
   private final Type[] actualTypeArguments;
   private final Type rawType;

   protected AbstractContainerEvent(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments)
   {
      this.errors = new ArrayList<Throwable>();
      this.beanManager = beanManager;
      this.actualTypeArguments = actualTypeArguments;
      this.rawType = rawType;
   }

   /**
    * @return the errors
    */
   protected List<Throwable> getErrors()
   {
      return errors;
   }
   
   protected BeanManagerImpl getBeanManager()
   {
      return beanManager;
   }
   
   protected void fire()
   {
      Type eventType = new ParameterizedTypeImpl(getRawType(), getEmptyTypeArray(), null);
      try
      {
         beanManager.fireEvent(eventType, this);
      }
      catch (Exception e) 
      {
         getErrors().add(new DefinitionException(e));
      }
   }

   protected Type getRawType()
   {
      return rawType;
   }

   protected Type[] getEmptyTypeArray()
   {
      return actualTypeArguments;
   }

}