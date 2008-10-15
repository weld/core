package org.jboss.webbeans.model;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.DependentAnnotationLiteral;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

/**
 * Web Beans component meta model for the container instantiated, injectable, 
 * observable events (Section 7.4).
 * 
 * @author David Allen
 *
 */
public class EventComponentModel<T> extends AbstractComponentModel<T, Object>
{
   private String location;
   private AnnotatedItem<Object> annotatedItem;
   private AnnotatedItem<Object> xmlAnnotatedItem;

   public EventComponentModel(SimpleAnnotatedItem<Object> annotatedItem, SimpleAnnotatedItem<Object> xmlAnnotatedItem, ManagerImpl manager)
   {
      this.annotatedItem = annotatedItem;
      this.xmlAnnotatedItem = xmlAnnotatedItem;
      this.init(manager);
   }

   @Override
   public ComponentConstructor<T> getConstructor()
   {
      // TODO No constructor is needed, but make sure this does not brake instantiation
      return null;
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
      // TODO Get the class for Event and use it for the type
      this.type = null;
   }

   @Override
   protected AnnotatedItem<Object> getAnnotatedItem()
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
   protected AnnotatedItem<Object> getXmlAnnotatedItem()
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
      this.deploymentType = new StandardAnnotationLiteral();
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
      this.scopeType = new DependentAnnotationLiteral();
   }
   
}
