package org.jboss.webbeans;

import static org.jboss.webbeans.ComponentMetaModel.ComponentType.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.BindingType;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Destroys;
import javax.webbeans.Initializer;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.bindings.ProductionBinding;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.injectable.ConstructorMetaModel;
import org.jboss.webbeans.injectable.MethodMetaModel;
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
public class ComponentMetaModel<T>
{
   
   public enum ComponentType
   {
      SIMPLE,
      ENTERPRISE;
   }
   
   public static final String LOGGER_NAME = "componentMetaModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private Class<? extends T> type;
   private Set<Annotation> bindingTypes;
   private Annotation deploymentType;
   private String name;
   private Annotation scopeType;
   private ComponentType componentType;
   private ConstructorMetaModel<T> constructor;
   private EjbMetaData<T> ejbMetaData;
   private MethodMetaModel<?> removeMethod;
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   @SuppressWarnings("unchecked")
   public ComponentMetaModel(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
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
      this.ejbMetaData = EJB.getEjbMetaData(type);
      this.componentType = initComponentType(type, ejbMetaData);
      checkComponentImplementation(componentType, type);
      this.constructor = initConstructor(type);
      MergedComponentStereotypes stereotypes = new MergedComponentStereotypes(annotatedItem, xmlAnnotatedItem, container);
      this.bindingTypes = initBindingTypes(annotatedItem, xmlAnnotatedItem);
      this.deploymentType = initDeploymentType(stereotypes, annotatedItem, xmlAnnotatedItem, container);
      this.scopeType = initScopeType(stereotypes, annotatedItem, xmlAnnotatedItem);
      this.name = initName(stereotypes, annotatedItem, xmlAnnotatedItem, componentType, type);
      this.removeMethod = initRemoveMethod(componentType, ejbMetaData, type);
      checkRequiredTypesImplemented(stereotypes, type);
      checkScopeAllowed(stereotypes, scopeType, type, componentType, ejbMetaData);
      // TODO Interceptors
   }
   
   /*
    * A series of static methods which implement the algorithms defined in the Web Beans spec for component meta data
    */
   
   @SuppressWarnings("unchecked")
   protected static <T> ConstructorMetaModel<T> initConstructor(Class<? extends T> type)
   {
      if (type.getConstructors().length == 1)
      {
         Constructor<T> constructor = type.getConstructors()[0];
         log.finest("Exactly one constructor (" + constructor +") defined, using it as the component constructor for " + type);
         return new ConstructorMetaModel<T>(constructor);
      }
      
      if (type.getConstructors().length > 1)
      {
         List<Constructor<T>> initializerAnnotatedConstructors = Reflections.getConstructors(type, Initializer.class);
         List<Constructor<T>> bindingTypeAnnotatedConstructors = Reflections.getConstructorsForMetaAnnotatedParameter(type, BindingType.class);
         log.finest("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + type);
         log.finest("Found " + bindingTypeAnnotatedConstructors + " with parameters annotated with binding types for " + type);
         if ((initializerAnnotatedConstructors.size() + bindingTypeAnnotatedConstructors.size()) > 1)
         {
            if (initializerAnnotatedConstructors.size() > 1)
            {
               throw new RuntimeException("Cannot have more than one constructor annotated with @Initializer for " + type);
            }
            
            else if (bindingTypeAnnotatedConstructors.size() > 1)
            {
               throw new RuntimeException("Cannot have more than one constructor with binding types specified on constructor parameters for " + type);
            }
            else
            {
               throw new RuntimeException("Specify a constructor either annotated with @Initializer or with parameters annotated with binding types for " + type);
            }
         }
         else if (initializerAnnotatedConstructors.size() == 1)
         {
            Constructor<T> constructor = initializerAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") annotated with @Initializer defined, using it as the component constructor for " + type);
            return new ConstructorMetaModel<T>(constructor);
         }
         else if (bindingTypeAnnotatedConstructors.size() == 1)
         {
            Constructor<T> constructor = bindingTypeAnnotatedConstructors.get(0);
            log.finest("Exactly one constructor (" + constructor +") with parameters annotated with binding types defined, using it as the component constructor for " + type);
            return new ConstructorMetaModel<T>(constructor);
         }
      }
      
      if (type.getConstructors().length == 0)
      {      
         Constructor<T> constructor = (Constructor<T>) Reflections.getConstructor(type);
         log.finest("No constructor defined, using implicit no arguement constructor for " + type);
         return new ConstructorMetaModel<T>(constructor);
      }
      
      throw new RuntimeException("Cannot determine constructor to use for " + type);
   }
   
   protected static <T> MethodMetaModel<?> initRemoveMethod(ComponentType componentType, EjbMetaData<T> ejbMetaData, Class<? extends T> type)
   {
      if (componentType.equals(ENTERPRISE) && ejbMetaData.isStateful())
      {
         if (ejbMetaData.getRemoveMethods().size() == 1)
         {
            return new MethodMetaModel<Object>(ejbMetaData.getRemoveMethods().get(0));
         }
         else if (ejbMetaData.getRemoveMethods().size() > 1)
         {
            List<Method> possibleRemoveMethods = new ArrayList<Method>();
            for (Method removeMethod : ejbMetaData.getRemoveMethods())
            {
               if (removeMethod.isAnnotationPresent(Destroys.class))
               {
                  possibleRemoveMethods.add(removeMethod);
               }
            }
            if (possibleRemoveMethods.size() == 1)
            {
               return new MethodMetaModel<Object>(possibleRemoveMethods.get(0)); 
            }
            else if (possibleRemoveMethods.size() > 1)
            {
               throw new RuntimeException("Multiple remove methods are annotated @Destroys for " + type);
            }
            else if (possibleRemoveMethods.size() == 0)
            {
               throw new RuntimeException("Multiple remove methods are declared, and none are annotated @Destroys for " + type);
            }
         }
         else if (ejbMetaData.getRemoveMethods().size() == 0)
         {
            throw new RuntimeException("Stateful enterprise bean component has no remove methods declared for " + type);
         }
      }
      else
      {
         List<Method> destroysMethods = Reflections.getMethods(type, Destroys.class);
         if (destroysMethods.size() > 0)
         {
            throw new RuntimeException("Only stateful enterprise bean components can have methods annotated @Destroys; " + type + " is not a stateful enterprise bean component");
         }
      }
      return null;
   }
   
   protected static <T> ComponentType initComponentType(Class<? extends T> type, EjbMetaData<T> ejbMetaData)
   {
      if (ejbMetaData != null && (ejbMetaData.isMessageDriven() || ejbMetaData.isSingleton() || ejbMetaData.isStateful() || ejbMetaData.isStateless()))
      {
         log.finest(type + " is an enterprise bean component");
         return ENTERPRISE;
      }
      else
      {
         log.finest(type + " is an simple component");
         return SIMPLE;
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
   protected static void checkScopeAllowed(MergedComponentStereotypes stereotypes, Annotation scopeType, Class<?> type, ComponentType componentType, EjbMetaData ejbMetaData)
   {
      log.finest("Checking if " + scopeType + " is allowed for " + type);
      if (stereotypes.getSupportedScopes().size() > 0)
      {
         if (!stereotypes.getSupportedScopes().contains(scopeType.annotationType()))
         {
            throw new RuntimeException("Scope " + scopeType + " is not an allowed by the stereotype for " + type);
         }
      }
      if (isDeclaredFinal(type) && !scopeType.annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("Scope " + scopeType + " is not allowed as the class is declared final or has methods declared final for " + type + ". Only @Dependent is allowed for final components");
      }
      if (componentType.equals(ComponentType.ENTERPRISE) && ejbMetaData.isStateless() && !scopeType.annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("Scope " + scopeType + " is not allowed on stateless enterpise bean components for " + type + ". Only @Dependent is allowed on stateless enterprise bean components");
      }
      if (componentType.equals(ComponentType.ENTERPRISE) && ejbMetaData.isSingleton() && (!scopeType.annotationType().equals(Dependent.class) || !scopeType.annotationType().equals(ApplicationScoped.class)))
      {
         throw new RuntimeException("Scope " + scopeType + " is not allowed on singleton enterpise bean components for " + type + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise bean components");
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

   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   public Annotation getDeploymentType()
   {
      return deploymentType;
   }

   public String getName()
   {
      return name;
   }

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
   
   public MethodMetaModel<?> getRemoveMethod()
   {
      return removeMethod;
   }

}
