package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentType;
import javax.webbeans.InterceptorBindingType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

/**
 * A meta model for a stereotype, allows us to cache a stereotype and to validate it
 * 
 * @author pmuir
 *
 */
public class StereotypeModel<T extends Annotation> extends AnnotationModel<T>
{
   private Annotation defaultDeploymentType;
   private Annotation defaultScopeType;
   private boolean beanNameDefaulted;
   private Set<Class<? extends Annotation>> supportedScopes;
   private Set<Class<?>> requiredTypes;
   private Set<Annotation> interceptorBindings;
   
   public StereotypeModel(Class<T> sterotype)
   {
      super(sterotype);
      initDefaultDeploymentType();
      initDefaultScopeType();
      initBeanNameDefaulted();
      initSupportedScopes();
      initRequiredTypes();
      initInterceptorBindings();
      checkBindingTypes();
   }
   
   private void checkBindingTypes()
   {
      Set<Annotation> bindingTypes = getAnnotatedAnnotation().getAnnotations(BindingType.class);
      if (bindingTypes.size() > 0)
      {
         throw new DefinitionException("Cannot declare binding types on a stereotype " + getAnnotatedAnnotation());
      }
   }

   private void initInterceptorBindings()
   {
      interceptorBindings = getAnnotatedAnnotation().getAnnotations(InterceptorBindingType.class);
   }

   private void initSupportedScopes()
   {
      this.supportedScopes = new HashSet<Class<? extends Annotation>>();
      Class<? extends Annotation>[] supportedScopes = getAnnotatedAnnotation().getAnnotation(Stereotype.class).supportedScopes();
      if (supportedScopes.length > 0)
      {
         this.supportedScopes.addAll(Arrays.asList(supportedScopes));
      }
   }
   
   private void initRequiredTypes()
   {
      this.requiredTypes = new HashSet<Class<?>>();
      Class<?>[] requiredTypes = getAnnotatedAnnotation().getAnnotation(Stereotype.class).requiredTypes();
      if (requiredTypes.length > 0)
      {
         this.requiredTypes.addAll(Arrays.asList(requiredTypes));
      }
   }

   private void initBeanNameDefaulted()
   {
      if (getAnnotatedAnnotation().isAnnotationPresent(Named.class))
      {
         if (!"".equals(getAnnotatedAnnotation().getAnnotation(Named.class).value()))
         {
            throw new DefinitionException("Cannot specify a value for a @Named stereotype " + getAnnotatedAnnotation());
         }
         beanNameDefaulted = true;
      }
   }

   private void initDefaultScopeType()
   {
      Set<Annotation> scopeTypes = getAnnotatedAnnotation().getAnnotations(ScopeType.class);
      if (scopeTypes.size() > 1)
      {
         throw new DefinitionException("At most one scope type may be specified for " + getAnnotatedAnnotation());
      }
      else if (scopeTypes.size() == 1)
      {
         this.defaultScopeType = scopeTypes.iterator().next();
      }
   }

   private void initDefaultDeploymentType()
   {
      Set<Annotation> deploymentTypes = getAnnotatedAnnotation().getAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new DefinitionException("At most one deployment type may be specified on " + getAnnotatedAnnotation());
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
    * Returns true if the stereotype specifies the bean name should be 
    * defaulted
    */
   public boolean isBeanNameDefaulted()
   {
      return beanNameDefaulted;
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
   
   @Deprecated
   public Class<? extends Annotation> getStereotypeClass()
   {
      return getType();
   }
   
   @Override
   public String toString()
   {
      return "StereotypeModel[" + getType() + "]";
   }

   @Override
   protected Class<? extends Annotation> getMetaAnnotation()
   {
      return Stereotype.class;
   }
   
}
