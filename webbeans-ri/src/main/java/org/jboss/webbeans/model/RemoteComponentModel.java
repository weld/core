package org.jboss.webbeans.model;

import javax.webbeans.BoundTo;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedType;

public class RemoteComponentModel<T> extends AbstractEnterpriseComponentModel<T>
{
   
   private EnterpriseConstructor<T> constructor;
   private InjectableMethod<?> removeMethod;
   private String boundTo;
   
   public RemoteComponentModel(AnnotatedType<T> annotatedItem,
         AnnotatedType<T> xmlAnnotatedItem)
   {
      super(annotatedItem, xmlAnnotatedItem);
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
   
   public InjectableMethod<?> getRemoveMethod()
   {
      return removeMethod;
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

}
