package org.jboss.webbeans.xml;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedAnnotationImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class XmlParserEnvironment
{
   
   private final List<AnnotatedClass<?>> classes;
   private final List<AnnotatedAnnotation<?>> annotations;
   private final Iterable<URL> webBeansXml;
   private final List<AnnotatedAnnotation<?>> enabledDeploymentTypes;
   private final ResourceLoader resourceLoader;
   
   public XmlParserEnvironment(ResourceLoader resourceLoader, Iterable<URL> webBeansXml)
   {
      this.classes = new ArrayList<AnnotatedClass<?>>();
      this.annotations = new ArrayList<AnnotatedAnnotation<?>>();
      this.enabledDeploymentTypes = new ArrayList<AnnotatedAnnotation<?>>();
      this.webBeansXml = webBeansXml;
      this.resourceLoader = resourceLoader;
   }
   
   public Iterable<URL> getWebBeansXml()
   {
      return webBeansXml;
   }
   
   public List<AnnotatedClass<?>> getClasses()
   {
      return classes;
   }
   
   public List<AnnotatedAnnotation<?>> getAnnotations()
   {
      return annotations;
   }
   
   public <T> AnnotatedClass<? extends T> loadClass(String className, Class<T> expectedType)
   {
      return AnnotatedClassImpl.of(resourceLoader.classForName(className).asSubclass(expectedType));
   }
   
   public <T extends Annotation> AnnotatedAnnotation<? extends T> loadAnnotation(String className, Class<T> expectedType)
   {
      return AnnotatedAnnotationImpl.of(resourceLoader.classForName(className).asSubclass(expectedType));
   }
   
   public List<AnnotatedAnnotation<?>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
}
