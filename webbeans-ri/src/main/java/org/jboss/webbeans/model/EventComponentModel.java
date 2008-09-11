package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;

import javax.webbeans.Container;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.bindings.StandardBinding;
import org.jboss.webbeans.introspector.AnnotatedType;

/**
 * Web Beans component meta model for the container instantiated, injectable, 
 * observable events (Section 7.2).
 * 
 * @author David Allen
 *
 */
public class EventComponentModel<T> extends SimpleComponentModel<T>
{

   private StandardBinding  deploymentType = new StandardBinding();
   private DependentBinding scopeType      = new DependentBinding();
   private ContainerImpl    container;

   /**
    * Creates a new component model for an injectable, observable event object.
    * @see org.jboss.webbeans.event.EventImpl
    * 
    * @param annotatedItem The injectable variable declared in Java
    * @param xmlAnnotatedItem The injectable variable defined in XML
    * @param container The Web Beans container
    */
   public EventComponentModel(AnnotatedType annotatedItem, AnnotatedType xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem, container);
      // This is needed later for the impl of Event to fire events with the container
      this.container = container;
   }

   /**
    * The implementation of the container used to create this model.
    * @return the container
    */
   public Container getContainer()
   {
      return container;
   }

   @Override
   public Annotation getDeploymentType()
   {
      // This is always @Standard per 7.2
      return deploymentType;
   }

   @Override
   public String getName()
   {
      // No name per 7.2
      return "";
   }

   @Override
   public Annotation getScopeType()
   {
      // This is always @Dependent per 7.2
      return scopeType;
   }

}
