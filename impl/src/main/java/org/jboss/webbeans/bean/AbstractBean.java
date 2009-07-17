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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.decorator.Decorates;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.Named;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.deployment.Standard;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.stereotype.Stereotype;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.conversation.ConversationImpl;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.literal.AnyLiteral;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.cache.MergedStereotypes;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
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

   private static final Annotation ANY_LITERAL = new AnyLiteral();
   private static final Annotation CURRENT_LITERAL = new CurrentLiteral();

   @SuppressWarnings("unchecked")
   private static Set<Class<?>> STANDARD_WEB_BEAN_CLASSES = new HashSet<Class<?>>(Arrays.asList(Event.class, BeanManagerImpl.class, ConversationImpl.class));

   private boolean proxyable;

   /**
    * Helper method for getting the highest precedence enabled deployment type
    * 
    * @param enabledDeploymentTypes The currently enabled deployment types
    * @param possibleDeploymentTypes The possible deployment types
    * @return The deployment type
    */
   public static Class<? extends Annotation> getDeploymentType(List<Class<? extends Annotation>> enabledDeploymentTypes, Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes)
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
   private final Log log = Logging.getLog(AbstractBean.class);
   // The binding types
   protected Set<Annotation> bindings;
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
   protected Set<WBInjectionPoint<?, ?>> injectionPoints;
   // If the type a primitive?
   private boolean primitive;
   // The Web Beans manager
   protected BeanManagerImpl manager;

   protected boolean _serializable;

   private boolean initialized;

   private Set<WBInjectionPoint<?, ?>> decoratesInjectionPoint;

   protected boolean isInitialized()
   {
      return initialized;
   }

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   public AbstractBean(BeanManagerImpl manager)
   {
      super(manager);
      this.manager = manager;
      injectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      mergedStereotypes = new MergedStereotypes<T, E>(getAnnotatedItem().getMetaAnnotations(Stereotype.class), manager);
      if (isSpecializing())
      {
         preSpecialize(environment);
         specialize(environment);
         postSpecialize();
      }
      initDefaultBindings();
      initPrimitive();
      log.trace("Building Web Bean bean metadata for #0", getType());
      initName();
      initDeploymentType();
      checkDeploymentType();
      initScopeType();
      initSerializable();
      initProxyable();
      initInjectionPoints();
      initDecorates();
      checkDecorates();
   }

   protected void checkDecorates()
   {
      if (this.decoratesInjectionPoint.size() > 0)
      {
         throw new DefinitionException("Cannot place @Decorates at an injection point which is not on a Decorator " + this);
      }
   }

   protected void initDecorates()
   {
      this.decoratesInjectionPoint = new HashSet<WBInjectionPoint<?, ?>>();
      for (WBInjectionPoint<?, ?> injectionPoint : getAnnotatedInjectionPoints())
      {
         if (injectionPoint.isAnnotationPresent(Decorates.class))
         {
            this.decoratesInjectionPoint.add(injectionPoint);
         }
      }
   }

   protected Set<WBInjectionPoint<?, ?>> getDecoratesInjectionPoint()
   {
      return decoratesInjectionPoint;
   }

   /**
    * Initializes the API types
    */
   protected void initTypes()
   {
      types = getAnnotatedItem().getTypeClosure();
   }

   /**
    * Initializes the binding types
    */
   protected void initBindings()
   {
      this.bindings = new HashSet<Annotation>();
      this.bindings.addAll(getAnnotatedItem().getMetaAnnotations(BindingType.class));
      initDefaultBindings();
      log.trace("Using binding types " + bindings + " specified by annotations");
   }

   protected abstract void initInjectionPoints();

   protected void initDefaultBindings()
   {
      if (bindings.size() == 0)
      {
         log.trace("Adding default @Current binding type");
         this.bindings.add(CURRENT_LITERAL);
      }
      this.bindings.add(ANY_LITERAL);
   }

   /**
    * Initializes the deployment types
    */
   protected abstract void initDeploymentType();

   protected void initDeploymentTypeFromStereotype()
   {
      Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes = getMergedStereotypes().getPossibleDeploymentTypes();
      if (possibleDeploymentTypes.size() > 0)
      {
         this.deploymentType = getDeploymentType(manager.getEnabledDeploymentTypes(), possibleDeploymentTypes);
         log.trace("Deployment type #0 specified by stereotype", deploymentType);
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
      proxyable = getAnnotatedItem().isProxyable();
   }

   /**
    * Initializes the primitive flag
    */
   protected void initPrimitive()
   {
      this.primitive = Reflections.isPrimitive(getType());
   }

   private boolean checkInjectionPointsAreSerializable()
   {
      boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(this.getScopeType()).isPassivating();
      for (WBInjectionPoint<?, ?> injectionPoint : getAnnotatedInjectionPoints())
      {
         Annotation[] bindings = injectionPoint.getMetaAnnotationsAsArray(BindingType.class);
         Bean<?> resolvedBean = manager.getBeans(injectionPoint.getJavaClass(), bindings).iterator().next();
         if (passivating)
         {
            if (Dependent.class.equals(resolvedBean.getScopeType()) && !Reflections.isSerializable(resolvedBean.getBeanClass()) && (((injectionPoint instanceof WBField) && !((WBField<?>) injectionPoint).isTransient()) || (injectionPoint instanceof WBParameter)))
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
    * Validates the deployment type
    */
   protected void checkDeploymentType()
   {
      if (deploymentType == null)
      {
         throw new DefinitionException("type: " + getType() + " must specify a deployment type");
      }
      else if (deploymentType.equals(Standard.class) && !STANDARD_WEB_BEAN_CLASSES.contains(getAnnotatedItem().getJavaClass()))
      {
         throw new DefinitionException(getAnnotatedItem().getName() + " cannot have deployment type @Standard");
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

   protected void preSpecialize(BeanDeployerEnvironment environment)
   {

   }

   protected void specialize(BeanDeployerEnvironment environment)
   {

   }

   /**
    * Returns the annotated time the bean represents
    * 
    * @return The annotated item
    */
   protected abstract WBAnnotated<T, E> getAnnotatedItem();

   /**
    * Gets the binding types
    * 
    * @return The set of binding types
    * 
    * @see org.jboss.webbeans.bean.BaseBean#getBindings()
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

   @Override
   public abstract AbstractBean<?, ?> getSpecializedBean();

   /**
    * Gets the deployment type of the bean
    * 
    * @return The deployment type
    * 
    * @see org.jboss.webbeans.bean.BaseBean#getDeploymentType()
    */
   public Class<? extends Annotation> getDeploymentType()
   {
      return deploymentType;
   }

   @Override
   public Set<WBInjectionPoint<?, ?>> getAnnotatedInjectionPoints()
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
    * @see org.jboss.webbeans.bean.BaseBean#getName()
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
    * @see org.jboss.webbeans.bean.BaseBean#getScopeType()
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
   @Override
   public Class<T> getType()
   {
      return type;
   }

   /**
    * Gets the API types of the bean
    * 
    * @return The set of API types
    * 
    * @see org.jboss.webbeans.bean.BaseBean#getTypeClosure()
    */
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
   public boolean isAssignableFrom(WBAnnotated<?, ?> annotatedItem)
   {
      return this.getAnnotatedItem().isAssignableFrom(annotatedItem);
   }

   /**
    * Indicates if bean is nullable
    * 
    * @return True if nullable, false otherwise
    * 
    * @see org.jboss.webbeans.bean.BaseBean#isNullable()
    */
   public boolean isNullable()
   {
      return !isPrimitive();
   }

   /**
    * Indicates if bean type is a primitive
    * 
    * @return True if primitive, false otherwise
    */
   @Override
   public boolean isPrimitive()
   {
      return primitive;
   }

   public boolean isSerializable()
   {
      // TODO WTF - why are we not caching the serializability of injection
      // points!
      return _serializable && checkInjectionPointsAreSerializable();
   }

   protected void initSerializable()
   {
      _serializable = Reflections.isSerializable(type);
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

   @Override
   public boolean isProxyable()
   {
      return proxyable;
   }

   @Override
   public boolean isDependent()
   {
      return Dependent.class.equals(getScopeType());
   }

   @Override
   public boolean isSpecializing()
   {
      return getAnnotatedItem().isAnnotationPresent(Specializes.class);
   }

}
