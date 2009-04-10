package org.jboss.webbeans.xml;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.ee.AbstractJavaEEResourceBean;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class XmlEnvironment
{
   
   private final List<AnnotatedClass<?>> classes;
   private final List<AnnotatedAnnotation<?>> annotations;
   private final ServiceRegistry serviceRegistry;
   private final List<Class<? extends Annotation>> enabledDeploymentTypes;
   private final Iterable<URL> beansXmlUrls;
   private final EjbDescriptorCache ejbDescriptors;
   private final Set<AbstractJavaEEResourceBean<?>> resourceBeans;
   private final ManagerImpl manager;
   
   public XmlEnvironment(ServiceRegistry serviceRegistry, EjbDescriptorCache ejbDescriptors, ManagerImpl manager)
   {
      this(serviceRegistry, serviceRegistry.get(WebBeanDiscovery.class).discoverWebBeansXml(), ejbDescriptors, manager);
   }
   
   protected XmlEnvironment(ServiceRegistry serviceRegistry, Iterable<URL> beanXmlUrls, EjbDescriptorCache ejbDescriptors, ManagerImpl manager)
   {
      this.classes = new ArrayList<AnnotatedClass<?>>();
      this.annotations = new ArrayList<AnnotatedAnnotation<?>>();
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      this.resourceBeans = new HashSet<AbstractJavaEEResourceBean<?>>();
      this.serviceRegistry = serviceRegistry;
      this.beansXmlUrls = beanXmlUrls;
      this.ejbDescriptors = ejbDescriptors;
      this.manager = manager;
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
      return serviceRegistry.get(ClassTransformer.class).classForName((serviceRegistry.get(ResourceLoader.class).classForName(className).asSubclass(expectedType)));
   }
   
   public <T extends Annotation> Class<? extends T> loadAnnotation(String className, Class<T> expectedType)
   {
      return serviceRegistry.get(ResourceLoader.class).classForName(className).asSubclass(expectedType);
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
   public EjbDescriptorCache getEjbDescriptors() 
   {
      return ejbDescriptors;
   }
   
   public URL loadFileByUrn(String urn, String fileName)
   {
      char separator = '/';
      String packageName = urn.replaceFirst(XmlConstants.URN_PREFIX, "");
      String path = packageName.replace('.', separator);
      String filePath = path + separator + fileName;
      return serviceRegistry.get(ResourceLoader.class).getResource(filePath);
   }
   
   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }
   
   public Set<AbstractJavaEEResourceBean<?>> getResourceBeans()
   {
      return resourceBeans;
   }
   
   public ManagerImpl getManager()
   {
      return manager;
   }
   
}
