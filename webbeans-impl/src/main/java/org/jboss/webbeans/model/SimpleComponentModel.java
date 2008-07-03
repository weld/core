package org.jboss.webbeans.model;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.util.AnnotatedItem;

public class SimpleComponentModel<T> extends AbstractComponentModel<T>
{
   
   private SimpleConstructor<T> constructor;
   private InjectableMethod<?> removeMethod;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   @SuppressWarnings("unchecked")
   public SimpleComponentModel(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem, container);
      this.constructor = initConstructor(getType());
      // TODO Interceptors
   }

   public SimpleConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   public InjectableMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

   
}
