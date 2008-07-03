package org.jboss.webbeans.model;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.util.AnnotatedItem;

public class RemoteComponentModel<T> extends AbstractComponentModel<T>
{
   
   public RemoteComponentModel(AnnotatedItem annotatedItem,
         AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem, container);
      // TODO Auto-generated constructor stub
   }

   @Override
   public ComponentConstructor<T> getConstructor()
   {
      // TODO Auto-generated method stub
      return null;
   }

   

}
