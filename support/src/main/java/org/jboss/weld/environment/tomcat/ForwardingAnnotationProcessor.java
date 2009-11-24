package org.jboss.weld.environment.tomcat;

import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

import org.apache.AnnotationProcessor;

public abstract class ForwardingAnnotationProcessor implements AnnotationProcessor
{
   
   protected abstract AnnotationProcessor delegate();
   
   public void postConstruct(Object instance) throws IllegalAccessException, InvocationTargetException
   {
      delegate().postConstruct(instance);
   }
   
   public void preDestroy(Object instance) throws IllegalAccessException, InvocationTargetException
   {
      delegate().preDestroy(instance);
   }
   
   public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      delegate().processAnnotations(instance);
   }
   
}
