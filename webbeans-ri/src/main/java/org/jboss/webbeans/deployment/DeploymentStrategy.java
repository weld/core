package org.jboss.webbeans.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.Container;
import javax.webbeans.DeploymentType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.ComponentInstanceImpl;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.jboss.webbeans.util.Reflections;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

/**
 * @author Pete Muir
 * @author Shane Bryzak
 *
 */
public class DeploymentStrategy
{
   private static final Logger log = LoggerUtil.getLogger("deploymentStrategy");     
   
   private ClassLoader classLoader;
   private ContainerImpl container;
   
   public DeploymentStrategy(ClassLoader classLoader, Container container)
   {
      this.classLoader = classLoader;
      
      if (!(container instanceof ContainerImpl))
      {
         throw new IllegalArgumentException("Container must be an instance of ContainerImpl");
      }
      
      this.container = (ContainerImpl) container;
   }
   
   public void scan(URL[] urls)
   {
           
      AnnotationDB db = new AnnotationDB();
      try
      {
         db.scanArchives(urls);
         db.crossReferenceMetaAnnotations();
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Epic fail", ex);
      }
      
      Map<String,Set<String>> index = db.getAnnotationIndex();
      addStereotypes(index);
      addComponents(index);
   }
   
   private void addStereotypes(Map<String, Set<String>> index)
   {
      Set<String> stereotypeClassNames = index.get(Stereotype.class.getName());
      try
      {
         for (String className : stereotypeClassNames)
         {
            log.info("Creating stereotype " + className);
            StereotypeModel stereotypeModel = new StereotypeModel(new ClassAnnotatedItem(Reflections.classForName(className)));
            container.getStereotypeManager().addStereotype(stereotypeModel);
            log.info("Stereotype: " + stereotypeModel);
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   }
   
   private void addComponents(Map<String, Set<String>> index)
   {
      Set<String> annotationNames = index.get(DeploymentType.class.getName());
      
      try
      {
         for (String annotationType : annotationNames)
         {
            Set<String> classNames = index.get(annotationType);
            for (String className : classNames)
            {
               log.finest("Creating componnt" + className);
               SimpleComponentModel componentModel = new SimpleComponentModel(
                     new ClassAnnotatedItem(Reflections.classForName(className)), 
                     new MutableAnnotatedItem(null, new HashMap()), container);  
               container.addComponent(new ComponentInstanceImpl(componentModel));
               log.info("Web Bean: " + componentModel);
            }
         }
      }
      catch (ClassNotFoundException ex)
      {
         throw new RuntimeException(ex);
      }
   }
   
   public void scan()
   {
      URL[] urls = ClasspathUrlFinder.findResourceBases("META-INF/web-beans.xml");
      scan(urls);
   }
   
   

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }
}