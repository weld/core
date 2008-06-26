package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class StereotypeManager
{
   
   // TODO Store these in the application context (when it exists)
   public static Map<Class<? extends Annotation>, StereotypeMetaModel> stereotypes = new HashMap<Class<? extends Annotation>, StereotypeMetaModel>();

   public void addStereotype(StereotypeMetaModel stereotype)
   {
      stereotypes.put(stereotype.getStereotypeClass(), stereotype);
   }
   
   public StereotypeMetaModel getStereotype(Class<? extends Annotation> annotationType)
   {
      return stereotypes.get(annotationType);
   }

}
