package org.jboss.webbeans.model;

import javax.webbeans.BoundTo;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.introspector.AnnotatedType;

public class RemoteComponentModel<T> extends AbstractEnterpriseComponentModel<T>
{
   
   private EnterpriseConstructor<T> constructor;
   private String boundTo;
   private String location;
   
   public RemoteComponentModel(AnnotatedType annotatedItem,
         AnnotatedType xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem);
      init(container);
   }
   
   @Override
   protected void init(ContainerImpl container)
   {
      super.init(container);
      // TODO initialize constructor
      initBoundTo();
   }
   
   protected void initBoundTo()
   {
      if (getXmlAnnotatedItem().isAnnotationPresent(BoundTo.class))
      {
         this.boundTo = getXmlAnnotatedItem().getAnnotation(BoundTo.class).value();
         return;
      }
      if (getAnnotatedItem().isAnnotationPresent(BoundTo.class))
      {
         this.boundTo = getAnnotatedItem().getAnnotation(BoundTo.class).value();
         return;
      }
      throw new RuntimeException("Remote component doesn't specify @BoundTo or <bound-to /> for " + getType());
   }
   
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }
   

   public String getBoundTo()
   {
      return boundTo;
   }
   
   @Override
   public String toString()
   {
      return "RemoteComponentModel[" + getType().getName() + "]";
   }

   @Override
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Remote Enterprise Component; declaring class: " + getType() +";";
      }
      return location;
   }

}
