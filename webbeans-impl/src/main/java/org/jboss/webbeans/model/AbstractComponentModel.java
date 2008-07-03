package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Destroys;
import javax.webbeans.Initializer;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.bindings.ProductionBinding;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
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
public abstract class AbstractComponentModel<T>
{

   public static final String LOGGER_NAME = "componentMetaModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private Class<? extends T> type;
   private Set<Annotation> bindingTypes;
   private Annotation deploymentType;
   private Annotation scopeType;
   private MergedStereotypesModel mergedStereotypes;

   private String name;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param container
    */
   @SuppressWarnings("unchecked")
   public AbstractComponentModel(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
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
      mergedStereotypes = new MergedStereotypesModel(annotatedItem, xmlAnnotatedItem, container);
      this.bindingTypes = initBindingTypes(annotatedItem, xmlAnnotatedItem);
      this.deploymentType = initDeploymentType(mergedStereotypes, annotatedItem, xmlAnnotatedItem, container);
      this.scopeType = initScopeType(mergedStereotypes, annotatedItem, xmlAnnotatedItem);
      this.name = initName(getMergedStereotypes(), annotatedItem, xmlAnnotatedItem, type);
      checkRequiredTypesImplemented(getMergedStereotypes(), type);
      checkScopeAllowed(getMergedStereotypes(), getScopeType(), type);
      checkComponentImplementation(type);
      // TODO Interceptors
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
   
   protected static Annotation initDeploymentType(MergedStereotypesModel stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
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
   
   /**
    * Return the scope of the component
    */
   protected static Annotation initScopeType(MergedStereotypesModel stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
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
   
   protected static String initName(MergedStereotypesModel stereotypes, AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, Class<?> type)
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
          name = Strings.decapitalize(type.getSimpleName());
         log.finest("Default name of " + type + " is " + name );
      }
      return name;
   }
   
   /**
    * Check that the types required by the stereotypes on the component are implemented
    */
   protected static void checkRequiredTypesImplemented(MergedStereotypesModel stereotypes, Class<?> type)
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
    * Check that the scope type is allowed by the stereotypes on the component and the component type
    * @param type 
    */
   protected static void checkScopeAllowed(MergedStereotypesModel stereotypes, Annotation scopeType, Class<?> type)
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
   
   protected static <T> InjectableMethod<?> initRemoveMethod(EjbMetaData<T> ejbMetaData, Class<? extends T> type)
   {
      if (ejbMetaData.isStateful())
      {
         if (ejbMetaData.getRemoveMethods().size() == 1)
         {
            return new InjectableMethod<Object>(ejbMetaData.getRemoveMethods().get(0));
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
               return new InjectableMethod<Object>(possibleRemoveMethods.get(0)); 
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
   
   protected static void checkComponentImplementation(Class<?> type)
   {
      if (Reflections.isAbstract(type))
      {
         throw new RuntimeException("Web Bean implementation class " + type + " cannot be declared abstract");
      }
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
   
   protected Class<? extends T> getType()
   {
      return type;
   }
   
   protected MergedStereotypesModel getMergedStereotypes()
   {
      return mergedStereotypes;
   }
   
   public abstract ComponentConstructor<T> getConstructor();

}
