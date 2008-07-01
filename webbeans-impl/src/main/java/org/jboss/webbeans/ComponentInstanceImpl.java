package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.bindings.ProductionBinding;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;

/**
 * Web Beans Component meta model
 * 
 * @author Pete Muir
 * 
 */
public class ComponentInstanceImpl<T> extends ComponentInstance<T>
{
   
   public enum ComponentType
   {
      SIMPLE,
      EJB_SESSION
      ;
   }
   
   public static final String LOGGER_NAME = "componentInstance";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private Class<? extends T> type;
   private Set<Annotation> bindingTypes;
   private Annotation deploymentType;
   private String name;
   private Annotation scopeType;
   private ComponentType componentType;
   private ConstructorMetaModel<T> constructor;
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   @SuppressWarnings("unchecked")
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
      
      this.type = (Class<? extends T>) initType(annotatedItem, xmlAnnotatedItem);
      log.fine("Building Web Bean component metadata for " +  type);
      this.componentType = initComponentType(type);
      checkComponentImplementation(componentType, type);
      this.constructor = new ConstructorMetaModel<T>(type);
      MergedComponentStereotypes stereotypes = new MergedComponentStereotypes(annotatedItem, xmlAnnotatedItem, container);
      this.bindingTypes = initBindingTypes(annotatedItem, xmlAnnotatedItem);
      this.deploymentType = initDeploymentType(stereotypes, annotatedItem, xmlAnnotatedItem, container);
      this.scopeType = initScopeType(stereotypes, annotatedItem, xmlAnnotatedItem);
      this.name = initName(stereotypes, annotatedItem, xmlAnnotatedItem, componentType, type);
      checkRequiredTypesImplemented(stereotypes, type);
      checkScopeAllowed(stereotypes, scopeType, type);
      // TODO Interceptors
   }
   
   /*
    * A series of static methods which implement the algorithms defined in the Web Beans spec for component meta data
    */
   
   protected static ComponentType initComponentType(Class<?> type)
   {
      if (EJB.isStatefulEjbComponent(type) || EJB.isStatelessEjbComponent(type))
      {
         return ComponentType.EJB_SESSION;
      }
      else
      {
         return ComponentType.SIMPLE;
      }
   }
   
   protected static void checkComponentImplementation(ComponentType componentType, Class<?> type)
   {
      switch (componentType)
      {
      case SIMPLE:
         checkSimpleComponentImplementation(type);
         break;
      }
   }
   
   protected static void checkSimpleComponentImplementation(Class<?> type)
   {
      if (Reflections.isAbstract(type))
      {
         throw new RuntimeException("Web Bean implementation class " + type + " cannot be declared abstract");
      }
   }
   
   protected static boolean isDeclaredFinal(Class<?> type)
   {
      if (Reflections.isFinal(type))
      {
         return true;
      }
      for (Method method : type.getDeclaredMethods())
      {
         if (Reflections.isFinal(method))
         {
            return true;
         }
      }
      return false;
   }

   @SuppressWarnings("unchecked")
   protected static Class<?> initType(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
   {
      if (annotatedItem.getAnnotatedClass() != null && xmlAnnotatedItem.getAnnotatedClass() != null && !annotatedItem.getAnnotatedClass().equals(xmlAnnotatedItem.getAnnotatedClass()))
      {
         throw new IllegalArgumentException("Cannot build a component which specifies different classes in XML and Java");
      }
      else if (xmlAnnotatedItem.getAnnotatedClass() != null)
      {
         log.finest("Component type specified in XML");
         return xmlAnnotatedItem.getAnnotatedClass();
      }
      else if (annotatedItem.getAnnotatedClass() != null)
      {
         log.finest("Component type specified in Java");
         return annotatedItem.getAnnotatedClass();
      }
      else
      {
         throw new IllegalArgumentException("Cannot build a component which doesn't specify a type");
      }
   }
   
   /**
    * Check that the scope type is allowed by the stereotypes on the component and the component type
    * @param type 
    */
   protected static void checkScopeAllowed(MergedComponentStereotypes stereotypes, Annotation scopeType, Class<?> type)
   {
      log.finest("Checking if " + scopeType + " is allowed");
      if (stereotypes.getSupportedScopes().size() > 0)
      {
         if (!stereotypes.getSupportedScopes().contains(scopeType.annotationType()))
         {
            throw new RuntimeException("Scope " + scopeType + " is not an allowed by the component's stereotype");
         }
      }
      if (isDeclaredFinal(type) && !scopeType.annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("Scope " + scopeType + " is not allowed as the component is declared final or has methods declared final");
      }
   }
   
   /**
    * Check that the types required by the stereotypes on the component are implemented
    */
   protected static void checkRequiredTypesImplemented(MergedComponentStereotypes stereotypes, Class<?> type)
   {
      for (Class<?> requiredType : stereotypes.getRequiredTypes())
      {
         log.finest("Checking if required type " + requiredType + " is implemented");
         if (!requiredType.isAssignableFrom(type))
         {
            throw new RuntimeException("Required type " + requiredType + " isn't implement on " + type);
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
         Annotation scope = xmlScopes.iterator().next();
         log.finest("Scope " + scope + " specified in XML");
         return scope;
      }
      
      Set<Annotation> scopes = annotatedItem.getAnnotations(ScopeType.class);
      if (scopes.size() > 1)
      {
         throw new RuntimeException("At most one scope may be specified");
      }
      
      if (scopes.size() == 1)
      {
         Annotation scope = scopes.iterator().next();
         log.finest("Scope " + scope + " specified b annotation");
         return scope;
      }
      
      if (stereotypes.getPossibleScopeTypes().size() == 1)
      {
         Annotation scope = stereotypes.getPossibleScopeTypes().iterator().next();
         log.finest("Scope " + scope + " specified by stereotype");
         return scope;
      }
      else if (stereotypes.getPossibleScopeTypes().size() > 1)
      {
         throw new RuntimeException("All stereotypes must specify the same scope OR a scope must be specified on the component");
      }
      
      log.finest("Using default @Dependent scope");
      return new DependentBinding();
   }

   protected static Annotation initDeploymentType(MergedComponentStereotypes stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      Set<Annotation> xmlDeploymentTypes = xmlAnnotatedItem.getAnnotations(DeploymentType.class);
      
      if (xmlDeploymentTypes.size() > 1)
      {
         throw new RuntimeException("At most one deployment type may be specified (" + xmlDeploymentTypes + " are specified)");
      }
      
      if (xmlDeploymentTypes.size() == 1)
      {
         Annotation deploymentType = xmlDeploymentTypes.iterator().next(); 
         log.finest("Deployment type " + deploymentType + " specified in XML");
         return deploymentType;
      }
      
      if (xmlAnnotatedItem.getAnnotatedClass() == null)
      {
      
         Set<Annotation> deploymentTypes = annotatedItem.getAnnotations(DeploymentType.class);
         
         if (deploymentTypes.size() > 1)
         {
            throw new RuntimeException("At most one deployment type may be specified (" + deploymentTypes + " are specified)");
         }
         if (deploymentTypes.size() == 1)
         {
            Annotation deploymentType = deploymentTypes.iterator().next();
            log.finest("Deployment type " + deploymentType + " specified by annotation");
            return deploymentType;
         }
      }
      
      if (stereotypes.getPossibleDeploymentTypes().size() > 0)
      {
         Annotation deploymentType = getDeploymentType(container.getEnabledDeploymentTypes(), stereotypes.getPossibleDeploymentTypes());
         log.finest("Deployment type " + deploymentType + " specified by stereotype");
         return deploymentType;
      }
      
      if (xmlAnnotatedItem.getAnnotatedClass() != null)
      {
         log.finest("Using default @Production deployment type");
         return new ProductionBinding();
      }
      throw new RuntimeException("All Java annotated classes have a deployment type");
   }

   protected static Set<Annotation> initBindingTypes(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
   {
      Set<Annotation> xmlBindingTypes = xmlAnnotatedItem.getAnnotations(BindingType.class);
      if (xmlBindingTypes.size() > 0)
      {
         // TODO support producer expression default binding type
         log.finest("Using binding types " + xmlBindingTypes + " specified in XML");
         return xmlBindingTypes;
      }
      
      Set<Annotation> bindingTypes = annotatedItem.getAnnotations(BindingType.class);
      
      if (bindingTypes.size() == 0)
      {
         log.finest("Adding default @Current binding type");
         bindingTypes.add(new CurrentBinding());
      }
      else
      {
         log.finest("Using binding types " + bindingTypes + " specified by annotations");
      }
      return bindingTypes;
   }

   protected static String initName(MergedComponentStereotypes stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ComponentType componentType, Class<?> type)
   {
      boolean componentNameDefaulted = false;
      String name = null;
      if (xmlAnnotatedItem.isAnnotationPresent(Named.class))
      {
         name = xmlAnnotatedItem.getAnnotation(Named.class).value();
         if ("".equals(name))
         {
            log.finest("Using default name (specified in XML)");
            componentNameDefaulted = true;
         }
         else
         {
            log.finest("Using name " + name + " specified in XML");
         }
      }
      else if (annotatedItem.isAnnotationPresent(Named.class))
      {
         name = annotatedItem.getAnnotation(Named.class).value();
         if ("".equals(name))
         {
            log.finest("Using default name (specified by annotations)");
            componentNameDefaulted = true;
         }
         else
         {
            log.finest("Using name " + name + " specified in XML");
         }
      }
      if ("".equals(name) && (componentNameDefaulted || stereotypes.isComponentNameDefaulted()))
      {
         if (ComponentType.SIMPLE.equals(componentType))
         {
            name = Strings.decapitalize(type.getSimpleName());
         }
         log.finest("Default name of " + type + " is " + name );
      }
      return name;
   }
   
   public static Annotation getDeploymentType(List<Annotation> enabledDeploymentTypes, Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes)
   {
      for (int i = (enabledDeploymentTypes.size() - 1); i > 0; i--)
      {
         if (possibleDeploymentTypes.containsKey((enabledDeploymentTypes.get(i).annotationType())))
         {
            return enabledDeploymentTypes.get(i); 
         }
      }
      return null;
   }

   @Override
   public T create(Container container)
   {
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
   public Annotation getDeploymentType()
   {
      return deploymentType;
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
   
   public ConstructorMetaModel<T> getConstructor()
   {
      return constructor;
   }
   
   public ComponentType getComponentType()
   {
      return componentType;
   }

}
