package org.jboss.webbeans.ejb;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.util.Reflections;


@SuppressWarnings("unchecked")
public class EJB
{
   
   private static Map<Class<?>, EjbMetaData> ejbMetaDataMap = new HashMap<Class<?>, EjbMetaData>();
   
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
   
   public static <T> EjbMetaData<T> getEjbMetaData(Class<? extends T> clazz)
   {
      // TODO replace with an application lookup
      if (!ejbMetaDataMap.containsKey(clazz))
      {
         EjbMetaData<T> ejbMetaData = new EjbMetaData(clazz); 
         ejbMetaDataMap.put(clazz, ejbMetaData);
         return ejbMetaData;
      }
      else
      {
         return ejbMetaDataMap.get(clazz);
      }
      
   }
   
}
