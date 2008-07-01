package org.jboss.webbeans.ejb;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.util.Reflections;


@SuppressWarnings("unchecked")
public class EJB
{
   
   public @interface Dummy {}
   
   public static final Class<Annotation> STATELESS;
   public static final Class<Annotation> STATEFUL;
   public static final Class<Annotation> MESSAGE_DRIVEN;
   
   static 
   {
      STATELESS = classForName("javax.ejb.Stateless");
      STATEFUL = classForName("javax.ejb.Stateful");
      MESSAGE_DRIVEN = classForName("javax.ejb.MessageDriven");
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
   
   public static boolean isStatelessEjbComponent(Class<?> clazz)
   {
      EjbMetaData ejbMetaData = getEjbMetaData(clazz);
      if (ejbMetaData != null)
      {
         return ejbMetaData.isStateless();
      }
      return false;
   }

   public static boolean isStatefulEjbComponent(Class<?> clazz)
   {
      EjbMetaData ejbMetaData = getEjbMetaData(clazz);
      if (ejbMetaData != null)
      {
         return ejbMetaData.isStateful();
      }
      return false;
   }
   
   public static boolean isMessageDrivenEjbComponent(Class<?> clazz)
   {
      EjbMetaData ejbMetaData = getEjbMetaData(clazz);
      if (ejbMetaData != null)
      {
         return ejbMetaData.isMessageDriven();
      }
      return false;
   }
   
   public static EjbMetaData getEjbMetaData(Class<?> clazz)
   {
      return null;
   }
   
}
