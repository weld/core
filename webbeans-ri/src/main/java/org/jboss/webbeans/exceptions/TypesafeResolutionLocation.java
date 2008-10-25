package org.jboss.webbeans.exceptions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.jboss.webbeans.injectable.Injectable;

public class TypesafeResolutionLocation extends Location
{
  
   private String target;
   
   private TypesafeResolutionLocation()
   {
      super("Typesafe resolution", null, null);
   }
   
   public TypesafeResolutionLocation(Injectable<?, ?> injectable)
   {
      this();
      this.target = injectable.getAnnotatedItem().toString();
   }

   public String getTarget()
   {
      return target;
   }
   
   public void setTarget(String target)
   {
      this.target = target;
   }
   
   @Override
   protected String getMessage()
   {
      String location = super.getMessage();
      if (getTarget() != null)
      {
         location += "target: " + getTarget() + "; "; 
      }
      return location;
   }
   
   public static String createMessage(Class<?> type, Type[] actualTypeArguements, Set<Annotation> annotations)
   {
      String string = type.toString();
      if (actualTypeArguements.length > 0)
      {
         string += "<";
         for (int i = 0; i < actualTypeArguements.length; i++)
         {
            string += actualTypeArguements[i].toString();
            if (i < actualTypeArguements.length - 1)
            {
               string += ",";
            }
         }
         string += ">";
      }
      string += annotations;
      return string;
   }
   
}
