package org.jboss.webbeans.deployment;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.Container;
import javax.webbeans.DeploymentType;

import org.jboss.webbeans.ComponentInstanceImpl;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.model.SimpleComponentModel;
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
   private static final Logger log = LoggerUtil.getLogger(DeploymentStrategy.class.getName());     
   
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
   
   public void scan()
   {
      URL[] urls = ClasspathUrlFinder.findResourceBases("META-INF/web-beans.xml");
           
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
      
      Set<String> classNames = index.get(DeploymentType.class.getName());
      
      try
      {
         for (String className : classNames)
         {
            SimpleComponentModel componentModel = new SimpleComponentModel(
                  new ClassAnnotatedItem(Reflections.classForName(className)), 
                  new MutableAnnotatedItem(null, new HashMap()), container);         
            container.addComponent(new ComponentInstanceImpl(componentModel));
         }
      }
      catch (ClassNotFoundException ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }
}