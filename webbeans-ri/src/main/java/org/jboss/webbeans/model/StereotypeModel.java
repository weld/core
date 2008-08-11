package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DeploymentType;
import javax.webbeans.InterceptorBindingType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedType;

/**
 * A meta model for a stereotype, allows us to cache a stereotype and to validate it
 * 
 * @author pmuir
 *
 */
public class StereotypeModel
{
   
   private Class<? extends Annotation> stereotypeClass;
   private Annotation defaultDeploymentType;
   private Annotation defaultScopeType;
   private boolean componentNameDefaulted;
   private Set<Class<? extends Annotation>> supportedScopes;
   private Set<Class<?>> requiredTypes;
   private Set<Annotation> interceptorBindings;
   
   @SuppressWarnings("unchecked")
   public StereotypeModel(AnnotatedType annotatedClass)
   {
      initStereotypeClass(annotatedClass);
      Stereotype stereotype = annotatedClass.getAnnotation(Stereotype.class);
      initDefaultDeploymentType(annotatedClass);
      initDefaultScopeType(annotatedClass);
      initComponentNameDefaulted(annotatedClass);
      initSupportedScopes(annotatedClass, stereotype);
      initRequiredTypes(annotatedClass, stereotype);
      initInterceptorBindings(annotatedClass);
      checkBindingTypes(annotatedClass);
   }
   
   private void checkBindingTypes(AnnotatedItem annotatedClass)
   {
      Set<Annotation> bindingTypes = annotatedClass.getAnnotations(BindingType.class);
      if (bindingTypes.size() > 0)
      {
         throw new RuntimeException("Cannot declare binding types on a stereotype " + annotatedClass);
      }
   }
   
   @SuppressWarnings("unchecked")
   private void initStereotypeClass(AnnotatedType annotatedClass)
   {
      if (Annotation.class.isAssignableFrom(annotatedClass.getAnnotatedClass()))
      {
         this.stereotypeClass = (Class<? extends Annotation>) annotatedClass.getAnnotatedClass();
      }
      else
      {
         throw new RuntimeException("@Stereotype can only be applied to an annotation, it was applied to " + annotatedClass);
      }
   }

   private void initInterceptorBindings(AnnotatedItem annotatedClass)
   {
      interceptorBindings = annotatedClass.getAnnotations(InterceptorBindingType.class);
   }

   private void initSupportedScopes(AnnotatedItem annotatedElement, Stereotype stereotype)
   {
      this.supportedScopes = new HashSet<Class<? extends Annotation>>();
      Class<? extends Annotation>[] supportedScopes = stereotype.supportedScopes();
      if (supportedScopes.length > 0)
      {
         this.supportedScopes.addAll(Arrays.asList(supportedScopes));
      }
   }
   
   private void initRequiredTypes(AnnotatedItem annotatedElement, Stereotype stereotype)
   {
      this.requiredTypes = new HashSet<Class<?>>();
      Class<?>[] requiredTypes = stereotype.requiredTypes();
      if (requiredTypes.length > 0)
      {
         this.requiredTypes.addAll(Arrays.asList(requiredTypes));
      }
   }

   private void initComponentNameDefaulted(AnnotatedItem annotatedElement)
   {
      if (annotatedElement.isAnnotationPresent(Named.class))
      {
         if (!"".equals(annotatedElement.getAnnotation(Named.class).value()))
         {
            throw new RuntimeException("Cannot specify a value for a @Named stereotype " + annotatedElement);
         }
         componentNameDefaulted = true;
      }
   }

   private void initDefaultScopeType(AnnotatedItem annotatedElement)
   {
      Set<Annotation> scopeTypes = annotatedElement.getAnnotations(ScopeType.class);
      if (scopeTypes.size() > 1)
      {
         throw new RuntimeException("At most one scope type may be specified for " + annotatedElement);
      }
      else if (scopeTypes.size() == 1)
      {
         this.defaultScopeType = scopeTypes.iterator().next();
      }
   }

   private void initDefaultDeploymentType(AnnotatedItem annotatedElement)
   {
      Set<Annotation> deploymentTypes = annotatedElement.getAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new RuntimeException("At most one deployment type may be specified on " + annotatedElement);
      }
      else if (deploymentTypes.size() == 1)
      {
         this.defaultDeploymentType = deploymentTypes.iterator().next();
      }
   }
   
   /**
    * Get the default deployment type the stereotype specifies, or null if none
    * is specified
    */
   public Annotation getDefaultDeploymentType()
   {
      return defaultDeploymentType;
   }
   
   /**
    * Get the default scope type the stereotype specifies, or null if none is
    * specified
    */
   public Annotation getDefaultScopeType()
   {
      return defaultScopeType;
   }
   
   /**
    * Get any interceptor bindings the the stereotype specifies, or an empty set
    * if none are specified
    */
   public Set<Annotation> getInterceptorBindings()
   {
      return interceptorBindings;
   }
   
   /**
    * Returns true if the stereotype specifies the component name should be 
    * defaulted
    */
   public boolean isComponentNameDefaulted()
   {
      return componentNameDefaulted;
   }
   
   /**
    * Returns the scopes this stereotype allows, or an empty set if none are 
    * specified
    */
   public Set<Class<? extends Annotation>> getSupportedScopes()
   {
      return supportedScopes;
   }
   
   /**
    * Returns the types this stereotype requires, or an empty set if none are
    * specified
    */
   public Set<Class<?>> getRequiredTypes()
   {
      return requiredTypes;
   }
   
   public Class<? extends Annotation> getStereotypeClass()
   {
      return stereotypeClass;
   }
   
   @Override
   public String toString()
   {
      return "StereotypeModel[" + stereotypeClass.getName() + "]";
   }
   
}
