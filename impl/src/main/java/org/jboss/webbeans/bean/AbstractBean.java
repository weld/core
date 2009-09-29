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
import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorates;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.stereotype.Stereotype;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.literal.AnyLiteral;
import org.jboss.webbeans.literal.DefaultLiteral;
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
   private static final Annotation CURRENT_LITERAL = new DefaultLiteral();

   private boolean proxyable;

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
   // Is it a policy, either defined by stereotypes or directly?
   private boolean policy;
   // The type
   protected Class<T> type;
   // The API types
   protected Set<Type> types;
   // The injection points
   private Set<WBInjectionPoint<?, ?>> injectionPoints;
   private Set<WBInjectionPoint<?, ?>> delegateInjectionPoints;
   // If the type a primitive?
   private boolean primitive;
   // The Web Beans manager
   protected BeanManagerImpl manager;

   private boolean _serializable;

   private boolean initialized;

   

   protected boolean isInitialized()
   {
      return initialized;
   }

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   public AbstractBean(String idSuffix, BeanManagerImpl manager)
   {
      super(idSuffix, manager);
      this.manager = manager;
      this.injectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      this.delegateInjectionPoints = new HashSet<WBInjectionPoint<?,?>>();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
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
      initScopeType();
      initSerializable();
      initProxyable();
      checkDelegateInjectionPoints();
   }
   
   protected void initStereotypes()
   {
      mergedStereotypes = new MergedStereotypes<T, E>(getAnnotatedItem().getMetaAnnotations(Stereotype.class), manager);
   }

   protected void checkDelegateInjectionPoints()
   {
      if (this.delegateInjectionPoints.size() > 0)
      {
         throw new DefinitionException("Cannot place @Decorates at an injection point which is not on a Decorator " + this);
      }
   }
   
   protected void addInjectionPoint(WBInjectionPoint<?, ?> injectionPoint)
   {
      if (injectionPoint.isAnnotationPresent(Decorates.class))
      {
         this.delegateInjectionPoints.add(injectionPoint);
      }
      injectionPoints.add(injectionPoint);
   }
   
   protected void addInjectionPoints(Iterable<? extends WBInjectionPoint<?, ?>> injectionPoints)
   {
      for (WBInjectionPoint<?, ?> injectionPoint : injectionPoints)
      {
         addInjectionPoint(injectionPoint);
      }
   }

   protected Set<WBInjectionPoint<?, ?>> getDelegateInjectionPoints()
   {
      return delegateInjectionPoints;
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
      this.bindings.addAll(getAnnotatedItem().getMetaAnnotations(Qualifier.class));
      initDefaultBindings();
      log.trace("Using binding types " + bindings + " specified by annotations");
   }

   protected void initDefaultBindings()
   {
      if (bindings.size() == 0)
      {
         log.trace("Adding default @Current binding type");
         this.bindings.add(CURRENT_LITERAL);
      }
      this.bindings.add(ANY_LITERAL);
   }

   protected void initPolicy()
   {
      if (getAnnotatedItem().isAnnotationPresent(Alternative.class))
      {
         this.policy = true;
      }
      else
      {
         this.policy = getMergedStereotypes().isPolicy();
      }
   }

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
      boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(this.getScope()).isPassivating();
      for (WBInjectionPoint<?, ?> injectionPoint : getAnnotatedInjectionPoints())
      {
         Annotation[] bindings = injectionPoint.getMetaAnnotationsAsArray(Qualifier.class);
         Bean<?> resolvedBean = manager.getBeans(injectionPoint.getJavaClass(), bindings).iterator().next();
         if (passivating)
         {
            if (Dependent.class.equals(resolvedBean.getScope()) && !Reflections.isSerializable(resolvedBean.getBeanClass()) && (((injectionPoint instanceof WBField<?, ?>) && !((WBField<?, ?>) injectionPoint).isTransient()) || (injectionPoint instanceof WBParameter<?, ?>)))
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

   protected void postSpecialize()
   {
      if (getAnnotatedItem().isAnnotationPresent(Named.class) && getSpecializedBean().getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         throw new DefinitionException("Cannot put name on specializing and specialized class " + getAnnotatedItem());
      }
      this.bindings.addAll(getSpecializedBean().getQualifiers());
      if (isSpecializing() && getSpecializedBean().getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         this.name = getSpecializedBean().getName();
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
    * @see org.jboss.webbeans.bean.BaseBean#getQualifiers()
    */
   public Set<Annotation> getQualifiers()
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
    * @see org.jboss.webbeans.bean.BaseBean#getScope()
    */
   public Class<? extends Annotation> getScope()
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

   @Override
   public boolean isProxyable()
   {
      return proxyable;
   }

   @Override
   public boolean isDependent()
   {
      return Dependent.class.equals(getScope());
   }
   
   public boolean isAlternative()
   {
      return policy;
   }

   @Override
   public boolean isSpecializing()
   {
      return getAnnotatedItem().isAnnotationPresent(Specializes.class);
   }

   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return mergedStereotypes.getStereotypes();
   }

}
