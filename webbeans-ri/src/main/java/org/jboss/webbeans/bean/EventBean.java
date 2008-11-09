package org.jboss.webbeans.bean;

import java.lang.reflect.Field;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedField;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

public class EventBean<T> extends AbstractBean<EventImpl<T>, Field>
{
   
   private static LogProvider log = Logging.getLogProvider(EventBean.class);
   
   private String location;
   private AnnotatedField<EventImpl<T>> annotatedItem;

   public EventBean(Field field, ManagerImpl manager)
   {
      super(manager);
      this.annotatedItem = new SimpleAnnotatedField<EventImpl<T>>(field);
      init();
   }

   /**
    * Caches the constructor for this type of bean to avoid future reflections during use.
    */
   @SuppressWarnings("unchecked")
   private void initConstructor()
   {
      try
      {
         //constructor = new SimpleConstructor<T>((Constructor<T>) EventImpl.class.getConstructor((Class[])null));
      } catch (Exception e)
      {
         log.warn("Unable to get constructor for build-in Event implementation", e);
      }
   }


   /*public BeanConstructor<T> getConstructor()
   {
      return constructor;
   }*/

   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Event Bean;";
      }
      return location;
   }

   @Override
   public String toString()
   {
      return "EventBean[" + getType().getName() + "]";
   }

   @Override
   protected void initType()
   {
      log.trace("Bean type specified in Java");
      this.type = annotatedItem.getType();
   }

    @Override
   protected AnnotatedItem<EventImpl<T>, Field> getAnnotatedItem()
   {
      return annotatedItem;
   }

   @Override
   protected String getDefaultName()
   {
      // No name per 7.4
      return null;
   }

   @Override
   protected void initDeploymentType()
   {
      // This is always @Standard per 7.4
      this.deploymentType = Standard.class;
   }

   @Override
   protected void checkDeploymentType()
   {
      // No - op
   }
   
   @Override
   protected void initName()
   {
      // No name per 7.4
      this.name = null;
   }

   @Override
   protected void initScopeType()
   {
      // This is always @Dependent per 7.4
      this.scopeType = Dependent.class;
   }
   
   @Override
   public EventImpl<T> create()
   {
      return new EventImpl<T>();
   }

   
}
