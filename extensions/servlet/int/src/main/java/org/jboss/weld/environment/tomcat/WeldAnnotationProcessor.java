package org.jboss.weld.environment.tomcat;

import org.apache.AnnotationProcessor;
import org.jboss.weld.environment.servlet.inject.AbstractInjector;
import org.jboss.weld.manager.api.WeldManager;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;

public class WeldAnnotationProcessor extends AbstractInjector implements AnnotationProcessor
{
   public WeldAnnotationProcessor(WeldManager manager)
   {
      super(manager);
   }

   public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      inject(instance);
   }

   public void postConstruct(Object arg0) throws IllegalAccessException, InvocationTargetException
   {
   }
   
   public void preDestroy(Object arg0) throws IllegalAccessException, InvocationTargetException
   {
   }
}
