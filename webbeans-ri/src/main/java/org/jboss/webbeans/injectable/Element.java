package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;

import javax.webbeans.Container;

public abstract class Element<T>
{
   
   private Annotation[] bindingTypes;
   
   public Element(Annotation[] bindingTypes)
   {
      this.bindingTypes = bindingTypes;
   }
   
   public Element()
   {
      this.bindingTypes = new Annotation[0];
   }

   public Annotation[] getBindingTypes()
   {
      return bindingTypes;
   }
   
   @Override
   public String toString()
   {
      return getType() + " with binding types " + getBindingTypes();
   }

   public T getValue(Container container)
   {
      return container.getInstanceByType(getType(), getBindingTypes());
   }
   
   public abstract Class<? extends T> getType();
   
}
