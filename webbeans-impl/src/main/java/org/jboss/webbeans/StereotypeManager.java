package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.model.StereotypeModel;

public class StereotypeManager
{
   
   // TODO Store these in the application context (when it exists)
   public static Map<Class<? extends Annotation>, StereotypeModel> stereotypes = new HashMap<Class<? extends Annotation>, StereotypeModel>();

   public void addStereotype(StereotypeModel stereotype)
   {
      stereotypes.put(stereotype.getStereotypeClass(), stereotype);
   }
   
   public StereotypeModel getStereotype(Class<? extends Annotation> annotationType)
   {
      return stereotypes.get(annotationType);
   }

}
