package org.jboss.webbeans.xml;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedAnnotationImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class XmlEnvironment
{
   
   private final List<AnnotatedClass<?>> classes;
   private final List<AnnotatedAnnotation<?>> annotations;
   private final ServiceRegistry serviceRegistry;
   private final List<Class<? extends Annotation>> enabledDeploymentTypes;
   private final Iterable<URL> beansXmlUrls;
   
   public XmlEnvironment(ServiceRegistry serviceRegistry)
   {
      this.classes = new ArrayList<AnnotatedClass<?>>();
      this.annotations = new ArrayList<AnnotatedAnnotation<?>>();
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      this.serviceRegistry = serviceRegistry;
      this.beansXmlUrls = serviceRegistry.get(WebBeanDiscovery.class).discoverWebBeansXml();
   }
   
   public List<AnnotatedClass<?>> getClasses()
   {
      return classes;
   }
   
   public List<AnnotatedAnnotation<?>> getAnnotations()
   {
      return annotations;
   }
   
   public Iterable<URL> getBeansXmlUrls()
   {
      return beansXmlUrls;
   }
   
   public <T> AnnotatedClass<? extends T> loadClass(String className, Class<T> expectedType)
   {
      return AnnotatedClassImpl.of(serviceRegistry.get(ResourceLoader.class).classForName(className).asSubclass(expectedType));
   }
   
   public <T extends Annotation> AnnotatedAnnotation<? extends T> loadAnnotation(String className, Class<T> expectedType)
   {
      return AnnotatedAnnotationImpl.of(serviceRegistry.get(ResourceLoader.class).classForName(className).asSubclass(expectedType));
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
}
