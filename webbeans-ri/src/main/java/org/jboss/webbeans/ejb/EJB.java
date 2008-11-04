package org.jboss.webbeans.ejb;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.util.Reflections;


public class EJB
{
   
   public @interface Dummy {}
   
   public static final Class<? extends Annotation> STATELESS_ANNOTATION;
   public static final Class<? extends Annotation> STATEFUL_ANNOTATION;
   public static final Class<? extends Annotation> MESSAGE_DRIVEN_ANNOTATION;
   public static final Class<? extends Annotation> SINGLETON_ANNOTATION;
   public static final Class<? extends Annotation> REMOVE_ANNOTATION;
   
   static 
   {
      STATELESS_ANNOTATION = classForName("javax.ejb.Stateless");
      STATEFUL_ANNOTATION = classForName("javax.ejb.Stateful");
      MESSAGE_DRIVEN_ANNOTATION = classForName("javax.ejb.MessageDriven");
// FIXME Faking singleton      
      SINGLETON_ANNOTATION = classForName("org.jboss.webbeans.test.annotations.Singleton");
//      SINGLETON_ANNOTATION = classForName("javax.ejb.Singleton");
      REMOVE_ANNOTATION = classForName("javax.ejb.Remove");
   }
   
   @SuppressWarnings("unchecked")
   private static Class<? extends Annotation> classForName(String name)
   {
      try
      {
         return (Class<? extends Annotation>) Reflections.classForName(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         return Dummy.class;
      }
   }
   
}
