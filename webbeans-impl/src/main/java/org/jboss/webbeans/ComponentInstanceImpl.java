package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.LoggerUtil;

/**
 * Web Beans Component meta model
 * 
 * @author Pete Muir
 * 
 */
public class ComponentInstanceImpl<T> extends ComponentInstance<T>
{
   
   
   
   public static final String LOGGER_NAME = "componentInstance";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private Class<?> type;
   private Set<Annotation> bindingTypes;
   private Annotation componentType;
   private String name;
   private Annotation scopeType;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   public ComponentInstanceImpl(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      if (annotatedItem == null)
      {
         throw new NullPointerException("annotatedItem must not be null. If the component is declared just in XML, pass in an empty annotatedItem");
      }
      
      if (xmlAnnotatedItem == null)
      {
         throw new NullPointerException("xmlAnnotatedItem must not be null. If the component is declared just in Java, pass in an empty xmlAnnotatedItem");
      }
      
      this.type = getType(annotatedItem, xmlAnnotatedItem);
      log.fine("Building Web Bean component metadata for " +  type);
      MergedComponentStereotypes stereotypes = new MergedComponentStereotypes(annotatedItem, xmlAnnotatedItem, container);
      this.bindingTypes = initBindingTypes(annotatedItem, xmlAnnotatedItem);
      this.componentType = initComponentType(stereotypes, annotatedItem, xmlAnnotatedItem, container);
      this.scopeType = initScopeType(stereotypes, annotatedItem, xmlAnnotatedItem);
      this.name = initName(stereotypes, annotatedItem, xmlAnnotatedItem);
      checkRequiredTypesImplemented(stereotypes, type);
      checkScopeAllowed(stereotypes, scopeType);
      // TODO Interceptors
   }
   
   /*
    * A series of static methods which implement the algorithms defined in the Web Beans spec for component meta data
    */
   
   protected static Class<?> getType(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
   {
      // TODO Consider XML type
      return annotatedItem.getAnnotatedClass();
   }
   
   /**
    * Check that the scope type is allowed by the stereotypes on the component
    */
   protected static void checkScopeAllowed(MergedComponentStereotypes stereotypes, Annotation scopeType)
   {
      if (stereotypes.getSupportedScopes().size() > 0)
      {
         if (!stereotypes.getSupportedScopes().contains(scopeType))
         {
            throw new RuntimeException("Scope " + scopeType + " is not an allowed by the component's stereotype");
         }
      }
   }
   
   /**
    * Check that the types required by the stereotypes on the component are implemented
    */
   protected static void checkRequiredTypesImplemented(MergedComponentStereotypes stereotypes, Class<?> type)
   {
      if (stereotypes.getRequiredTypes().size() > 0)
      {
         // TODO This needs to check a lot more. Or we do through checking assignability
         List<Class> classes = Arrays.asList(type.getInterfaces());
         if (!classes.containsAll(stereotypes.getRequiredTypes()))
         {
            // TODO Ugh, improve this exception
            throw new RuntimeException("Not all required types are implemented");
         }
      }
   }

   /**
    * Return the scope of the component
    */
   protected static Annotation initScopeType(MergedComponentStereotypes stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
   {
      Set<Annotation> xmlScopes = xmlAnnotatedItem.getAnnotations(ScopeType.class);
      if (xmlScopes.size() > 1)
      {
         throw new RuntimeException("At most one scope may be specified in XML");
      }
      
      if (xmlScopes.size() == 1)
      {
         log.info("Scope specified in XML");
         return xmlScopes.iterator().next();
      }
      
      Set<Annotation> scopes = annotatedItem.getAnnotations(ScopeType.class);
      if (scopes.size() > 1)
      {
         throw new RuntimeException("At most one scope may be specified");
      }
      
      if (scopes.size() == 1)
      {
         log.info("Scope specified by annotation");
         return scopes.iterator().next();
      }
      
      if (stereotypes.getPossibleScopeTypes().size() > 0)
      {
         return stereotypes.getPossibleScopeTypes().iterator().next();
      }
      
      return new DependentBinding();
   }

   protected static Annotation initComponentType(MergedComponentStereotypes stereotypes, AnnotatedItem annotatedElement, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      /*
       *  TODO deployment types actually identify components to deploy - and so 
       *  if declared in XML and java then there are two components to deploy - 
       *  this needs to be handled at a higher level
       *  
       *  TODO Ignore deployment type annotations on class if declared in XML
       */
      Set<Annotation> xmlDeploymentTypes = annotatedElement.getAnnotations(DeploymentType.class);
      
      if (xmlDeploymentTypes.size() > 1)
      {
         throw new RuntimeException("At most one deployment type may be specified in XML");
      }
      
      if (xmlDeploymentTypes.size() == 1)
      {
         return xmlDeploymentTypes.iterator().next();
      }
      
      Set<Annotation> deploymentTypes = annotatedElement.getAnnotations(DeploymentType.class);
      
      if (deploymentTypes.size() > 1)
      {
         // TODO Improve the exception
         throw new RuntimeException("At most one deployment type may be specified");
      }
      if (deploymentTypes.size() == 1)
      {
         return deploymentTypes.iterator().next();
      }
      
      if (stereotypes.getPossibleDeploymentTypes().size() > 0)
      {
         return getDeploymentType(container.getEnabledDeploymentTypes(), stereotypes.getPossibleDeploymentTypes());
      }
      
      // TODO If declared in XML then we can return Production here
      // TODO We shouldn't get here, but what to do if we have?
      return null;
   }

   protected static Set<Annotation> initBindingTypes(AnnotatedItem annotatedElement, AnnotatedItem xmlAnnotatedItem)
   {
      Set<Annotation> xmlBindingTypes = xmlAnnotatedItem.getAnnotations(BindingType.class);
      if (xmlBindingTypes.size() > 0)
      {
         // TODO support producer expression default binding type
         return xmlBindingTypes;
      }
      
      Set<Annotation> bindingTypes = annotatedElement.getAnnotations(BindingType.class);
      if (bindingTypes.size() == 0)
      {
         bindingTypes.add(new CurrentBinding());
      }
      return bindingTypes;
   }

   protected static String initName(MergedComponentStereotypes stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
   {
      boolean componentNameDefaulted = false;
      String name = null;
      if (xmlAnnotatedItem.isAnnotationPresent(Named.class))
      {
         name = xmlAnnotatedItem.getAnnotation(Named.class).value();
         if ("".equals(name))
         {
            componentNameDefaulted = true;
         }
      }
      else if (annotatedItem.isAnnotationPresent(Named.class))
      {
         name = annotatedItem.getAnnotation(Named.class).value();
         if ("".equals(name))
         {
            componentNameDefaulted = true;
         }
      }
      if ("".equals(name) && (componentNameDefaulted || stereotypes.isComponentNameDefaulted()))
      {
         // TODO Write default name alogorithm
      }
      return name;
   }
   
   public static Annotation getDeploymentType(List<Annotation> enabledDeploymentTypes, Set<Annotation> possibleDeploymentTypes)
   {
      List<Annotation> l = new ArrayList<Annotation>(enabledDeploymentTypes);
      l.retainAll(possibleDeploymentTypes);
      if (l.size() > 0)
      {
         return l.get(0);
      }
      else
      {
         return null;
      }
   }

   @Override
   public T create(Container container)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void destroy(Container container, Object instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   @Override
   public Annotation getComponentType()
   {
      return componentType;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Set<Class> getTypes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Annotation getScopeType()
   {
      return scopeType;
   }
   
   

}
