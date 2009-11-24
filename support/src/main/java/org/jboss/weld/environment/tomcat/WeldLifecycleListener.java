package org.jboss.weld.environment.tomcat;

import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.AnnotationProcessor;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;

public class WeldLifecycleListener implements LifecycleListener
{
   
   private static final AnnotationProcessor DUMMY_PROCESSOR = new AnnotationProcessor()
   {

      public void postConstruct(Object arg0) throws IllegalAccessException, InvocationTargetException {}

      public void preDestroy(Object arg0) throws IllegalAccessException, InvocationTargetException {}

      public void processAnnotations(Object arg0) throws IllegalAccessException, InvocationTargetException, NamingException {}
      
   };
   
   public void lifecycleEvent(LifecycleEvent event)
   {
      if (event.getType().equals("after_start") && event.getLifecycle() instanceof StandardContext)
      {
         StandardContext context = (StandardContext) event.getLifecycle();
         final ServletContext servletContext = context.getServletContext();
         
         // Initialize servlet injection
         final AnnotationProcessor originalAnnotationProcessor = context.getAnnotationProcessor();
         context.setAnnotationProcessor(new ForwardingAnnotationProcessor()
         {
            
            private AnnotationProcessor processor;

            @Override
            protected AnnotationProcessor delegate()
            {
               return originalAnnotationProcessor;
            }
            
            @Override
            public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
            {
               super.processAnnotations(instance);
               getProcessor().processAnnotations(instance);
            }
            
            @Override
            public void postConstruct(Object instance) throws IllegalAccessException, InvocationTargetException
            {
               super.postConstruct(instance);
               getProcessor().postConstruct(instance);
            }
            
            @Override
            public void preDestroy(Object instance) throws IllegalAccessException, InvocationTargetException
            {
               super.preDestroy(instance);
               getProcessor().preDestroy(instance);
            }
            
            private AnnotationProcessor getProcessor()
            {
               if (processor == null)
               {
                  Object o = servletContext.getAttribute("org.jboss.weld.environment.tomcat.WeldAnnotationProcessor");
                  if (o instanceof AnnotationProcessor)
                  {
                     processor = (AnnotationProcessor) o;
                  }
                  else
                  {
                     return DUMMY_PROCESSOR;
                  }
               }
               return processor;
            }
            
         });
      }
   }
   
}
