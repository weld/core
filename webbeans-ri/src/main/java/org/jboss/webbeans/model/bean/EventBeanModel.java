package org.jboss.webbeans.model.bean;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.BeanConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedField;
import org.jboss.webbeans.util.LoggerUtil;

/**
 * Web Beans bean meta model for the container instantiated, injectable, 
 * observable events (Section 7.4).
 * 
 * @author David Allen
 *
 */
public class EventBeanModel<T> extends AbstractBeanModel<T, Field>
{
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private String location;
   private SimpleAnnotatedField<T> annotatedItem;
   private SimpleAnnotatedField<T> xmlAnnotatedItem;
   //private BeanConstructor<T>      constructor;

   public EventBeanModel(SimpleAnnotatedField<T> annotatedItem, SimpleAnnotatedField<T> xmlAnnotatedItem, ManagerImpl manager)
   {
      this.annotatedItem = annotatedItem;
      this.xmlAnnotatedItem = xmlAnnotatedItem;
      initConstructor();
      this.init(manager);
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
         log.log(Level.SEVERE, "Unable to get constructor for build-in Event implementation", e);
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
      // TODO This is not the right way to check XML definition
      if (getXmlAnnotatedItem().getDelegate() != null)
      {
         log.finest("Bean type specified in XML");
         this.type = xmlAnnotatedItem.getType();
      }
      else if (getAnnotatedItem().getDelegate() != null)
      {
         log.finest("Bean type specified in Java");
         this.type = annotatedItem.getType();
      }
   }

   @Override
   protected AnnotatedItem<T, Field> getAnnotatedItem()
   {
      return this.annotatedItem;
   }

   @Override
   protected String getDefaultName()
   {
      // No name per 7.4
      return null;
   }

   @Override
   protected AnnotatedItem<T, Field> getXmlAnnotatedItem()
   {
      return this.xmlAnnotatedItem;
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

   public BeanConstructor<T, ?> getConstructor()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
