package org.jboss.webbeans.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.SimpleAnnotatedField;
import org.jboss.webbeans.util.LoggerUtil;

/**
 * Web Beans component meta model for the container instantiated, injectable, 
 * observable events (Section 7.4).
 * 
 * @author David Allen
 *
 */
public class EventComponentModel<T> extends AbstractComponentModel<T, Field>
{
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private String location;
   private SimpleAnnotatedField<T> annotatedItem;
   private SimpleAnnotatedField<T> xmlAnnotatedItem;
   private ComponentConstructor<T> constructor;

   public EventComponentModel(SimpleAnnotatedField<T> annotatedItem, SimpleAnnotatedField<T> xmlAnnotatedItem, ManagerImpl manager)
   {
      this.annotatedItem = annotatedItem;
      this.xmlAnnotatedItem = xmlAnnotatedItem;
      this.init(manager);
   }

   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      this.initConstructor();
   }

   /**
    * Initializes the constructor field of this class.
    */
   protected void initConstructor()
   {
      // There should only be one constructor for the event implementation used here
      // TODO Probably don't need to use reflection as this is a container supplied class
      Constructor<T> classConstructor = this.annotatedItem.getType().getConstructors()[0];
      constructor = new SimpleConstructor<T>(classConstructor);
   }

   @Override
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }

   @Override
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Event Component;";
      }
      return location;
   }

   @Override
   public String toString()
   {
      return "EventComponentModel[" + getType().getName() + "]";
   }

   /* (non-Javadoc)
    * @see org.jboss.webbeans.model.AbstractClassComponentModel#initType()
    */
   @Override
   protected void initType()
   {
      if (getXmlAnnotatedItem().getDelegate() != null)
      {
         log.finest("Component type specified in XML");
         this.type = xmlAnnotatedItem.getType();
      } else if (getAnnotatedItem().getDelegate() != null)
      {
         log.finest("Component type specified in Java");
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

   /* (non-Javadoc)
    * @see org.jboss.webbeans.model.AbstractComponentModel#initDeploymentType(org.jboss.webbeans.ManagerImpl)
    */
   @Override
   protected void initDeploymentType(ManagerImpl container)
   {
      // This is always @Standard per 7.4
      this.deploymentType = Standard.class;
   }

   /* (non-Javadoc)
    * @see org.jboss.webbeans.model.AbstractComponentModel#initName()
    */
   @Override
   protected void initName()
   {
      // No name per 7.4
      this.name = null;
   }

   /* (non-Javadoc)
    * @see org.jboss.webbeans.model.AbstractComponentModel#initScopeType()
    */
   @Override
   protected void initScopeType()
   {
      // This is always @Dependent per 7.4
      this.scopeType = Dependent.class;
   }
   
}
