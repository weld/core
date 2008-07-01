package org.jboss.webbeans;

import java.lang.annotation.Annotation;

public abstract class AbstractInjectedThingMetaModel
{
   
   private Annotation[] bindingTypes;
   
   public AbstractInjectedThingMetaModel(Annotation[] bindingTypes)
   {
      this.bindingTypes = bindingTypes;
   }
   
   public AbstractInjectedThingMetaModel()
   {
      this.bindingTypes = new Annotation[0];
   }

   public Annotation[] getBindingTypes()
   {
      return bindingTypes;
   }
   
   public abstract Class<?> getType();
   
   @Override
   public String toString()
   {
      return getType() + " with binding types " + getBindingTypes();
   }

}
