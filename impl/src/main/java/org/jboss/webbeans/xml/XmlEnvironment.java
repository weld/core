package org.jboss.webbeans.xml;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;

public interface XmlEnvironment
{
   
   public List<AnnotatedClass<?>> getClasses();
   
   public List<AnnotatedAnnotation<?>> getAnnotations();
   
   public Iterable<URL> getBeansXmlUrls();
   
   public <T> AnnotatedClass<? extends T> loadClass(String className, Class<T> expectedType);
   
   public <T extends Annotation> AnnotatedAnnotation<? extends T> loadAnnotation(String className, Class<T> expectedType);
   
   public List<Class<? extends Annotation>> getEnabledDeploymentTypes();
   
}