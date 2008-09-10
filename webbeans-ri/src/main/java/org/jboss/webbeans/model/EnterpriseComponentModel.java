package org.jboss.webbeans.model;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.introspector.AnnotatedType;

public class EnterpriseComponentModel<T> extends AbstractEnterpriseComponentModel<T>
{

   private EnterpriseConstructor<T> constructor;
   
   private String location;
   
   public EnterpriseComponentModel(AnnotatedType<T> annotatedItem,
         AnnotatedType<T> xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem);
      init(container);
   }
   
   @Override
   protected void init(ContainerImpl container)
   {
      super.init(container);
      this.constructor = new EnterpriseConstructor<T>(getEjbMetaData());
   }
   
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   @Override
   public String toString()
   {
      return "EnterpriseComponentModel[" + getType().getName() + "]";
   }

   @Override
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Enterprise Component; declaring class: " + getType() +";";
      }
      return location;
   }

}
