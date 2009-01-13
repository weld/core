/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Event;
import javax.webbeans.InjectionPoint;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;
import javax.webbeans.Specializes;
import javax.webbeans.Standard;
import javax.webbeans.Stereotype;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.binding.CurrentBinding;
import org.jboss.webbeans.injection.InjectionPointImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMember;
import org.jboss.webbeans.introspector.jlr.AbstractAnnotatedItem.AnnotationMap;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.model.MergedStereotypes;
import org.jboss.webbeans.util.Reflections;

/**
 * An abstract bean representation common for all beans
 * 
 * @author Pete Muir
 * 
 * @param <T>
 * @param <E>
 */
public abstract class AbstractBean<T, E> extends Bean<T>
{

   @SuppressWarnings("unchecked")
   private static Set<Class<?>> STANDARD_WEB_BEAN_CLASSES = new HashSet<Class<?>>(Arrays.asList(Event.class, ManagerImpl.class));

   /**
    * Helper class for getting deployment type
    * 
    * Loops through the enabled deployment types (backwards) and returns the
    * first one present in the possible deployments type, resulting in the
    * deployment type of highest priority
    * 
    * @param enabledDeploymentTypes The currently enabled deployment types
    * @param possibleDeploymentTypes The possible deployment types
    * @return The deployment type
    */
   public static Class<? extends Annotation> getDeploymentType(List<Class<? extends Annotation>> enabledDeploymentTypes, AnnotationMap possibleDeploymentTypes)
   {
      for (int i = (enabledDeploymentTypes.size() - 1); i > 0; i--)
      {
         if (possibleDeploymentTypes.containsKey((enabledDeploymentTypes.get(i))))
         {
            return enabledDeploymentTypes.get(i);
         }
      }
      return null;
   }

   // Logger
   private LogProvider log = Logging.getLogProvider(AbstractBean.class);
   // The binding types
   private Set<Annotation> bindingTypes;
   // The name
   protected String name;
   // The scope type
   protected Class<? extends Annotation> scopeType;
   // The merged stereotypes
   private MergedStereotypes<T, E> mergedStereotypes;
   // The deployment type
   protected Class<? extends Annotation> deploymentType;
   // The type
   protected Class<T> type;
   // The API types
   protected Set<Type> types;
   // The injection points
   protected Set<AnnotatedItem<?, ?>> annotatedInjectionPoints;
   // If the type a primitive?
   private boolean primitive;
   // The Web Beans manager
   protected ManagerImpl manager;
   // Cached values
   private Type declaredBeanType;

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   public AbstractBean(ManagerImpl manager)
   {
      super(manager);
      this.manager = manager;
      annotatedInjectionPoints = new HashSet<AnnotatedItem<?, ?>>();
   }

   /**
    * Initializes the bean and its metadata
    */
   protected void init()
   {
      mergedStereotypes = new MergedStereotypes<T, E>(getAnnotatedItem().getMetaAnnotations(Stereotype.class));
      initType();
      initPrimitive();
      log.debug("Building Web Bean bean metadata for " + getType());
      initBindingTypes();
      initName();
      initDeploymentType();
      checkDeploymentType();
      initScopeType();
      initTypes();
   }

   /**
    * Initializes the API types
    */
   protected void initTypes()
   {
      types = new HashSet<Type>();
      Reflections.getTypeHierachy(getType(), types);
   }

   /**
    * Initializes the binding types
    */
   protected void initBindingTypes()
   {
      this.bindingTypes = new HashSet<Annotation>();
      boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
      this.bindingTypes.addAll(getAnnotatedItem().getMetaAnnotations(BindingType.class));
      if (specialization)
      {
         this.bindingTypes.addAll(getSpecializedType().getBindings());
         log.trace("Using binding types " + bindingTypes + " specified by annotations and specialized supertype");
      }
      else if (bindingTypes.size() == 0)
      {
         log.trace("Adding default @Current binding type");
         this.bindingTypes.add(new CurrentBinding());
      }
      else
      {
         log.trace("Using binding types " + bindingTypes + " specified by annotations");
      }
      return;
   }

   /**
    * Initializes the deployment types
    */
   protected void initDeploymentType()
   {
      Set<Annotation> deploymentTypes = getAnnotatedItem().getMetaAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new DefinitionException("At most one deployment type may be specified (" + deploymentTypes + " are specified) on " + getAnnotatedItem().toString());
      }
      if (deploymentTypes.size() == 1)
      {
         this.deploymentType = deploymentTypes.iterator().next().annotationType();
         log.trace("Deployment type " + deploymentType + " specified by annotation");
         return;
      }

      AnnotationMap possibleDeploymentTypes = getMergedStereotypes().getPossibleDeploymentTypes();
      if (possibleDeploymentTypes.size() > 0)
      {
         this.deploymentType = getDeploymentType(manager.getEnabledDeploymentTypes(), possibleDeploymentTypes);
         log.trace("Deployment type " + deploymentType + " specified by stereotype");
         return;
      }

      this.deploymentType = getDefaultDeploymentType();
      log.trace("Using default @Production deployment type");
      return;
   }

   /**
    * Gets the default deployment type
    * 
    * @return The default deployment type
    */
   protected abstract Class<? extends Annotation> getDefaultDeploymentType();

   /**
    * Initializes the name
    */
   protected void initName()
   {
      boolean beanNameDefaulted = false;
      boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
      if (getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         if (specialization)
         {
            throw new DefinitionException("Name specified for specialized bean");
         }
         String javaName = getAnnotatedItem().getAnnotation(Named.class).value();
         if ("".equals(javaName))
         {
            log.trace("Using default name (specified by annotations)");
            beanNameDefaulted = true;
         }
         else
         {
            log.trace("Using name " + javaName + " specified by annotations");
            this.name = javaName;
            return;
         }
      }
      else if (specialization)
      {
         this.name = getSpecializedType().getName();
         log.trace("Using supertype name");
         return;
      }

      if (beanNameDefaulted || getMergedStereotypes().isBeanNameDefaulted())
      {
         this.name = getDefaultName();
         return;
      }
   }

   /**
    * Initializes the primitive flag
    */
   protected void initPrimitive()
   {
      this.primitive = Reflections.isPrimitive(getType());
   }

   protected boolean injectionPointsAreSerializable()
   {
      // TODO: a bit crude, don't check *all* injectionpoints, only those listed
      // in the spec for passivation checks
      for (AnnotatedItem<?, ?> injectionPoint : getAnnotatedInjectionPoints())
      {
         Annotation[] bindings = injectionPoint.getMetaAnnotationsAsArray(BindingType.class);
         Bean<?> resolvedBean = manager.resolveByType(injectionPoint.getType(), bindings).iterator().next();
         if (Dependent.class.equals(resolvedBean.getScopeType()) && !resolvedBean.isSerializable())
         {
            return false;
         }
      }
      return true;
   }

   /**
    * Initializes the scope type
    */
   protected void initScopeType()
   {
      Set<Annotation> scopeAnnotations = getAnnotatedItem().getMetaAnnotations(ScopeType.class);
      if (scopeAnnotations.size() > 1)
      {
         throw new DefinitionException("At most one scope may be specified");
      }
      if (scopeAnnotations.size() == 1)
      {
         this.scopeType = scopeAnnotations.iterator().next().annotationType();
         log.trace("Scope " + scopeType + " specified by annotation");
         return;
      }

      Set<Annotation> possibleScopeTypes = getMergedStereotypes().getPossibleScopeTypes();
      if (possibleScopeTypes.size() == 1)
      {
         this.scopeType = possibleScopeTypes.iterator().next().annotationType();
         log.trace("Scope " + scopeType + " specified by stereotype");
         return;
      }
      else if (possibleScopeTypes.size() > 1)
      {
         throw new DefinitionException("All stereotypes must specify the same scope OR a scope must be specified on the bean");
      }
      this.scopeType = Dependent.class;
      log.trace("Using default @Dependent scope");
   }

   /**
    * Initializes the type of the bean
    */
   protected abstract void initType();

   /**
    * Validates the deployment type
    */
   protected void checkDeploymentType()
   {
      if (deploymentType == null)
      {
         throw new DefinitionException("type: " + getType() + " must specify a deployment type");
      }
      else if (deploymentType.equals(Standard.class) && !STANDARD_WEB_BEAN_CLASSES.contains(getAnnotatedItem().getType()))
      {
         throw new DefinitionException(getAnnotatedItem().getName() + " cannot have deployment type @Standard");
      }
   }

   /**
    * Binds the decorators to the proxy
    */
   protected void bindDecorators()
   {
      // TODO Implement decorators
   }

   /**
    * Binds the interceptors to the proxy
    */
   protected void bindInterceptors()
   {
      // TODO Implement interceptors
   }

   /**
    * Returns the annotated time the bean represents
    * 
    * @return The annotated item
    */
   protected abstract AnnotatedItem<T, E> getAnnotatedItem();

   /**
    * Gets the binding types
    * 
    * @return The set of binding types
    * 
    * @see javax.webbeans.manager.Bean#getBindings()
    */
   public Set<Annotation> getBindings()
   {
      return bindingTypes;
   }

   /**
    * Gets the declared bean type
    * 
    * @return The bean type
    */
   protected Type getDeclaredBeanType()
   {
      if (declaredBeanType == null)
      {
         Type type = getClass();
         if (type instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getActualTypeArguments().length == 1)
            {
               declaredBeanType = parameterizedType.getActualTypeArguments()[0];
            }
         }
      }
      return declaredBeanType;
   }

   /**
    * Gets the default name of the bean
    * 
    * @return The default name
    */
   protected abstract String getDefaultName();

   /**
    * Gets the deployment type of the bean
    * 
    * @return The deployment type
    * 
    * @see javax.webbeans.manager.Bean#getDeploymentType()
    */
   public Class<? extends Annotation> getDeploymentType()
   {
      return deploymentType;
   }

   /**
    * Gets the injection points of the bean
    * 
    * @return The set of injection points
    */
   public Set<AnnotatedItem<?, ?>> getAnnotatedInjectionPoints()
   {
      return annotatedInjectionPoints;
   }

   public Set<InjectionPoint> getInjectionPoints()
   {
      Set<InjectionPoint> injectionsPoints = new HashSet<InjectionPoint>();
      for (AnnotatedItem<?, ?> annotatedInjectionPoint : annotatedInjectionPoints)
      {
         AnnotatedMember<?, ?> member = (AnnotatedMember<?, ?>) annotatedInjectionPoint;
         injectionsPoints.add(InjectionPointImpl.of(member, this));
      }
      return injectionsPoints;
   }

   /**
    * Gets the merged stereotypes of the bean
    * 
    * @return The set of merged stereotypes
    */
   public MergedStereotypes<T, E> getMergedStereotypes()
   {
      return mergedStereotypes;
   }

   /**
    * Gets the name of the bean
    * 
    * @return The name
    * 
    * @see javax.webbeans.manager.Bean#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Gets the scope type of the bean
    * 
    * @return The scope type
    * 
    * @see javax.webbeans.manager.Bean#getScopeType()
    */
   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   /**
    * Gets the specializes type of the bean
    * 
    * @return The specialized type
    */
   protected AbstractBean<? extends T, E> getSpecializedType()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the type of the bean
    * 
    * @return The type
    */
   public Class<T> getType()
   {
      return type;
   }

   /**
    * Gets the API types of the bean
    * 
    * @return The set of API types
    * 
    * @see javax.webbeans.manager.Bean#getTypes()
    */
   @Override
   public Set<Type> getTypes()
   {
      return types;
   }

   /**
    * Checks if this beans annotated item is assignable from another annotated
    * item
    * 
    * @param annotatedItem The other annotation to check
    * @return True if assignable, otherwise false
    */
   public boolean isAssignableFrom(AnnotatedItem<?, ?> annotatedItem)
   {
      return this.getAnnotatedItem().isAssignableFrom(annotatedItem);
   }

   /**
    * Indicates if bean is nullable
    * 
    * @return True if nullable, false otherwise
    * 
    * @see javax.webbeans.manager.Bean#isNullable()
    */
   @Override
   public boolean isNullable()
   {
      return !isPrimitive();
   }

   /**
    * Indicates if bean type is a primitive
    * 
    * @return True if primitive, false otherwise
    */
   public boolean isPrimitive()
   {
      return primitive;
   }

   @Override
   public boolean isSerializable()
   {
      // TODO: OK?
      return true;
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return "AbstractBean " + getName();
   }
}
