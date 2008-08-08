package org.jboss.webbeans.ejb;

import java.lang.annotation.Annotation;


import org.jboss.webbeans.util.Reflections;


@SuppressWarnings("unchecked")
public class EJB
{
   
   public @interface Dummy {}
   
   public static final Class<Annotation> STATELESS_ANNOTATION;
   public static final Class<Annotation> STATEFUL_ANNOTATION;
   public static final Class<Annotation> MESSAGE_DRIVEN_ANNOTATION;
   public static final Class<Annotation> SINGLETON_ANNOTATION;
   public static final Class<Annotation> REMOVE_ANNOTATION;
   
   static 
   {
      STATELESS_ANNOTATION = classForName("javax.ejb.Stateless");
      STATEFUL_ANNOTATION = classForName("javax.ejb.Stateful");
      MESSAGE_DRIVEN_ANNOTATION = classForName("javax.ejb.MessageDriven");
      SINGLETON_ANNOTATION = classForName("javax.ejb.Singleton");
      REMOVE_ANNOTATION = classForName("javax.ejb.Remove");
   }
   
   private static Class classForName(String name)
   {
      try
      {
         return Reflections.classForName(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         return Dummy.class;
      }
   }
   
}
