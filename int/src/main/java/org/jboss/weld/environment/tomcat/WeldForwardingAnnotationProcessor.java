package org.jboss.weld.environment.tomcat;

import org.apache.AnnotationProcessor;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.environment.servlet.util.Reflections;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 *
 * Forwards all calls in turn to two delegates: first to
 * originalAnnotationProcessor, then to weldProcessor.
 */
public class WeldForwardingAnnotationProcessor extends ForwardingAnnotationProcessor
{
   private final AnnotationProcessor firstProcessor;
   private final AnnotationProcessor secondProcessor;

   public WeldForwardingAnnotationProcessor(AnnotationProcessor originalAnnotationProcessor, AnnotationProcessor weldProcessor)
   {
      this.firstProcessor = originalAnnotationProcessor;
      this.secondProcessor = weldProcessor;
   }

   @Override                   
   protected AnnotationProcessor delegate()
   {
      return firstProcessor;
   }

   @Override
   public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      super.processAnnotations(instance);
      secondProcessor.processAnnotations(instance);
   }

   @Override
   public void postConstruct(Object instance) throws IllegalAccessException, InvocationTargetException
   {
      super.postConstruct(instance);
      secondProcessor.postConstruct(instance);
   }

   @Override
   public void preDestroy(Object instance) throws IllegalAccessException, InvocationTargetException
   {
      super.preDestroy(instance);
      secondProcessor.preDestroy(instance);
   }

   public static void replaceAnnotationProcessor(ServletContextEvent sce, WeldManager manager)
   {
      StandardContext stdContext = getStandardContext(sce);
      stdContext.setAnnotationProcessor(createInstance(manager, stdContext));
   }

   private static WeldForwardingAnnotationProcessor createInstance(WeldManager manager, StandardContext stdContext)
   {
      try
      {
         Class<?> clazz = Reflections.classForName(WeldAnnotationProcessor.class.getName());
         AnnotationProcessor weldProcessor = (AnnotationProcessor) clazz.getConstructor(WeldManager.class).newInstance(manager);
         return new WeldForwardingAnnotationProcessor(stdContext.getAnnotationProcessor(), weldProcessor);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot create WeldForwardingAnnotationProcessor", e);
      }
   }

   private static StandardContext getStandardContext(ServletContextEvent sce)
   {
      try
      {
         // Hack into Tomcat to replace the AnnotationProcessor using reflection to access private fields
         ApplicationContext appContext = (ApplicationContext) getContextFieldValue((ApplicationContextFacade)sce.getServletContext(), ApplicationContextFacade.class);
         return (StandardContext) getContextFieldValue(appContext, ApplicationContext.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot get StandardContext from ServletContext", e);
      }
   }

   private static <E> Object getContextFieldValue(E obj, Class<E> clazz)
           throws NoSuchFieldException, IllegalAccessException
   {
      Field f = clazz.getDeclaredField("context");
      f.setAccessible(true);
      return f.get(obj);
   }

   public static void restoreAnnotationProcessor(ServletContextEvent sce)
   {
      StandardContext stdContext = getStandardContext(sce);
      AnnotationProcessor ap = stdContext.getAnnotationProcessor();
      if (ap instanceof WeldForwardingAnnotationProcessor)
      {
         stdContext.setAnnotationProcessor(((WeldForwardingAnnotationProcessor)ap).firstProcessor);
      }
   }
}