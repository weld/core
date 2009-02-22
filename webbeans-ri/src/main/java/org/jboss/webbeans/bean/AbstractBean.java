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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Named;
import javax.annotation.Stereotype;
import javax.context.Dependent;
import javax.event.Event;
import javax.inject.BindingType;
import javax.inject.DefinitionException;
import javax.inject.Specializes;
import javax.inject.Standard;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.AnnotationStore.AnnotationMap;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.MergedStereotypes;
import org.jboss.webbeans.metadata.MetaDataCache;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Reflections;

/**
 * An abstract bean representation common for all beans
 * 
 * @author Pete Muir
 * 
 * @param <T> the type of bean
 * @param <E> the Class<?> of the bean type
 */
public abstract class AbstractBean<T, E> extends RIBean<T>
{

   @SuppressWarnings("unchecked")
   private static Set<Class<?>> STANDARD_WEB_BEAN_CLASSES = new HashSet<Class<?>>(Arrays.asList(Event.class, ManagerImpl.class));

   private boolean proxyable;

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
   private Set<Annotation> bindings;
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
   protected Set<AnnotatedInjectionPoint<?, ?>> injectionPoints;
   // If the type a primitive?
   private boolean primitive;
   // The Web Beans manager
   protected ManagerImpl manager;

   protected boolean _serializable;

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   public AbstractBean(ManagerImpl manager)
   {
      super(manager);
      this.manager = manager;
      injectionPoints = new HashSet<AnnotatedInjectionPoint<?, ?>>();
   }

   /**
    * Initializes the bean and its metadata
    */
   protected void init()
   {
      mergedStereotypes = new MergedStereotypes<T, E>(getAnnotatedItem().getMetaAnnotations(Stereotype.class));
      initBindings();
      if (isSpecializing())
      {
         preSpecialize();
         specialize();
         postSpecialize();
      }
      initDefaultBindings();
      initType();
      initPrimitive();
      if (log.isDebugEnabled())
         log.debug("Building Web Bean bean metadata for " + getType());
      initName();
      initDeploymentType();
      checkDeploymentType();
      initScopeType();
      initTypes();
      initSerializable();
      initProxyable();
      checkRequiredTypesImplemented();
   }

   /**
    * Initializes the API types
    */
   protected void initTypes()
   {
      types = new Reflections.HierarchyDiscovery<Type>(getAnnotatedItem().getUnderlyingType()).getFlattenedTypes();
   }

   /**
    * Initializes the binding types
    */
   protected void initBindings()
   {
      this.bindings = new HashSet<Annotation>();
      this.bindings.addAll(getAnnotatedItem().getMetaAnnotations(BindingType.class));
   }

   protected void initDefaultBindings()
   {
      if (bindings.size() == 0)
      {
         log.trace("Adding default @Current binding type");
         this.bindings.add(new CurrentLiteral());
      }
      else
      {
         if (log.isTraceEnabled())
            log.trace("Using binding types " + bindings + " specified by annotations");
      }
   }

   /**
    * Initializes the deployment types
    */
   protected abstract void initDeploymentType();

   protected void initDeploymentTypeFromStereotype()
   {
      AnnotationMap possibleDeploymentTypes = getMergedStereotypes().getPossibleDeploymentTypes();
      if (possibleDeploymentTypes.size() > 0)
      {
         this.deploymentType = getDeploymentType(manager.getEnabledDeploymentTypes(), possibleDeploymentTypes);
         if (log.isTraceEnabled())
            log.trace("Deployment type " + deploymentType + " specified by stereotype");
         return;
      }
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
      if (getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         String javaName = getAnnotatedItem().getAnnotation(Named.class).value();
         if ("".equals(javaName))
         {
            log.trace("Using default name (specified by annotations)");
            beanNameDefaulted = true;
         }
         else
         {
            if (log.isTraceEnabled())
               log.trace("Using name " + javaName + " specified by annotations");
            this.name = javaName;
            return;
         }
      }

      if (beanNameDefaulted || getMergedStereotypes().isBeanNameDefaulted())
      {
         this.name = getDefaultName();
         return;
      }
   }

   protected void initProxyable()
   {
      proxyable = Beans.apiTypesAreProxyable(getTypes());
   }

   /**
    * Initializes the primitive flag
    */
   protected void initPrimitive()
   {
      this.primitive = Reflections.isPrimitive(getType());
   }

   protected boolean checkInjectionPointsAreSerializable()
   {
      // TODO CACHE THIS and rebuild on addBean
      // TODO: a bit crude, don't check *all* injectionpoints, only those listed
      // in the spec for passivation checks
      for (AnnotatedItem<?, ?> injectionPoint : getInjectionPoints())
      {
         Annotation[] bindings = injectionPoint.getMetaAnnotationsAsArray(BindingType.class);
         Bean<?> resolvedBean = manager.resolveByType(injectionPoint.getType(), bindings).iterator().next();
         if (MetaDataCache.instance().getScopeModel(this.getScopeType()).isPassivating())
         {
            if (Dependent.class.equals(resolvedBean.getScopeType()) && !resolvedBean.isSerializable() && (((injectionPoint instanceof AnnotatedField) && !((AnnotatedField<?>) injectionPoint).isTransient()) || (injectionPoint instanceof AnnotatedParameter)) )
            {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * Initializes the scope type
    */
   protected abstract void initScopeType();

   protected boolean initScopeTypeFromStereotype()
   {
      Set<Annotation> possibleScopeTypes = getMergedStereotypes().getPossibleScopeTypes();
      if (possibleScopeTypes.size() == 1)
      {
         this.scopeType = possibleScopeTypes.iterator().next().annotationType();
         if (log.isTraceEnabled())
            log.trace("Scope " + scopeType + " specified by stereotype");
         return true;
      }
      else if (possibleScopeTypes.size() > 1)
      {
         throw new DefinitionException("All stereotypes must specify the same scope OR a scope must be specified on " + getAnnotatedItem());
      }
      else
      {
         return false;
      }
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
    * Validates that the required types are implemented
    */
   protected void checkRequiredTypesImplemented()
   {
      for (Class<?> requiredType : getMergedStereotypes().getRequiredTypes())
      {
         if (log.isTraceEnabled())
            log.trace("Checking if required type " + requiredType + " is implemented");
         if (!requiredType.isAssignableFrom(type))
         {
            throw new DefinitionException("Required type " + requiredType + " isn't implemented on " + type);
         }
      }
   }

   protected void postSpecialize()
   {
      if (getAnnotatedItem().isAnnotationPresent(Named.class) && getSpecializedBean().getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         throw new DefinitionException("Cannot put name on specializing and specialized class " + getAnnotatedItem());
      }
      this.bindings.addAll(getSpecializedBean().getBindings());
      if (isSpecializing() && getSpecializedBean().getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         this.name = getSpecializedBean().getName();
         return;
      }
      manager.getSpecializedBeans().put(getSpecializedBean(), this);
   }

   protected void preSpecialize()
   {

   }

   protected void specialize()
   {

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
    * @see javax.inject.manager.Bean#getBindings()
    */
   public Set<Annotation> getBindings()
   {
      return bindings;
   }

   /**
    * Gets the default name of the bean
    * 
    * @return The default name
    */
   protected abstract String getDefaultName();

   public abstract AbstractBean<?, ?> getSpecializedBean();

   /**
    * Gets the deployment type of the bean
    * 
    * @return The deployment type
    * 
    * @see javax.inject.manager.Bean#getDeploymentType()
    */
   public Class<? extends Annotation> getDeploymentType()
   {
      return deploymentType;
   }

   public Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints()
   {
      return injectionPoints;
   }

   /**
    * Gets the merged stereotypes of the bean
    * 
    * @return The set of merged stereotypes
    */
   protected MergedStereotypes<T, E> getMergedStereotypes()
   {
      return mergedStereotypes;
   }

   /**
    * Gets the name of the bean
    * 
    * @return The name
    * 
    * @see javax.inject.manager.Bean#getName()
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
    * @see javax.inject.manager.Bean#getScopeType()
    */
   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
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
    * @see javax.inject.manager.Bean#getTypes()
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
    * @see javax.inject.manager.Bean#isNullable()
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
      return _serializable;
   }

   protected void initSerializable()
   {
      _serializable = isPrimitive() || getTypes().contains(Serializable.class);
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

   public boolean isProxyable()
   {
      return proxyable;
   }

   public boolean isDependent()
   {
      return Dependent.class.equals(getScopeType());
   }

   public boolean isSpecializing()
   {
      return getAnnotatedItem().isAnnotationPresent(Specializes.class);
   }

   @Override
   // TODO Fix this!!!
   public boolean equals(Object other)
   {
      if (other instanceof AbstractBean)
      {
         AbstractBean<?, ?> that = (AbstractBean<?, ?>) other;
         boolean equal = this.getTypes().equals(that.getTypes()) && this.getBindings().equals(that.getBindings());
         return equal;
      }
      else
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + getTypes().hashCode();
      result = 31 * result + getBindings().hashCode();
      return result;
   }

}
