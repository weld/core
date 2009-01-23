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
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.Production;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentInstancesStore;
import org.jboss.webbeans.injection.InjectionPointProvider;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;

/**
 * An abstract bean representation common for class-based beans
 * 
 * @author Pete Muir
 * 
 * @param <T>
 * @param <E>
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>>
{
   // Logger
   private static final LogProvider log = Logging.getLogProvider(AbstractClassBean.class);
   // The item representation
   protected AnnotatedClass<T> annotatedItem;
   // The injectable fields
   private Set<AnnotatedField<?>> injectableFields;
   // The initializer methods
   private Set<AnnotatedMethod<?>> initializerMethods;
   protected DependentInstancesStore dependentInstancesStore;

   /**
    * Constructor
    * 
    * @param type The type
    * @param manager The Web Beans manager
    */
   protected AbstractClassBean(AnnotatedClass<T> type, ManagerImpl manager)
   {
      super(manager);
      this.annotatedItem = type;
      this.dependentInstancesStore = new DependentInstancesStore();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      checkScopeAllowed();
      checkBeanImplementation();
      // TODO Interceptors
      initInitializerMethods();
   }

   /**
    * Injects bound fields
    * 
    * @param instance The instance to inject into
    */
   protected void injectBoundFields(T instance)
   {
      InjectionPointProvider injectionPointProvider = manager.getInjectionPointProvider();
      for (AnnotatedField<?> injectableField : getInjectableFields())
      {
         injectionPointProvider.pushInjectionPoint(injectableField);
         try
         {
            injectableField.inject(instance, manager);
         }
         finally
         {
            injectionPointProvider.popInjectionPoint();
         }
      }
   }

   /**
    * Initializes the bean type
    */
   protected void initType()
   {
      log.trace("Bean type specified in Java");
      this.type = getAnnotatedItem().getType();
   }

   /**
    * Initializes the injection points
    */
   protected void initInjectionPoints()
   {
      injectableFields = new HashSet<AnnotatedField<?>>();
      for (AnnotatedField<?> annotatedField : annotatedItem.getMetaAnnotatedFields(BindingType.class))
      {
         if (!annotatedField.isAnnotationPresent(Produces.class))
         {
            if (annotatedField.isStatic())
            {
               throw new DefinitionException("Don't place binding annotations on static fields " + annotatedField);
            }
            if (annotatedField.isFinal())
            {
               throw new DefinitionException("Don't place binding annotations on final fields " + annotatedField);
            }
            injectableFields.add(annotatedField);
            super.annotatedInjectionPoints.add(annotatedField);
         }
      }
   }

   /**
    * Initializes the initializer methods
    */
   protected void initInitializerMethods()
   {
      initializerMethods = new HashSet<AnnotatedMethod<?>>();
      for (AnnotatedMethod<?> annotatedMethod : annotatedItem.getAnnotatedMethods(Initializer.class))
      {
         if (annotatedMethod.isStatic())
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be static");
         }
         else if (annotatedMethod.getAnnotation(Produces.class) != null)
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Produces");
         }
         else if (annotatedMethod.getAnnotatedParameters(Disposes.class).size() > 0)
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot have parameters annotated @Disposes");
         }
         else if (annotatedMethod.getAnnotatedParameters(Observes.class).size() > 0)
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Observes");
         }
         else
         {
            initializerMethods.add(annotatedMethod);
         }
      }
   }

   @Override
   protected void initScopeType()
   {
      for (AnnotatedClass<?> clazz = getAnnotatedItem(); clazz != null; clazz = clazz.getSuperclass())
      {
         Set<Annotation> scopeTypes = clazz.getDeclaredMetaAnnotations(ScopeType.class);
         scopeTypes = clazz.getDeclaredMetaAnnotations(ScopeType.class);
         if (scopeTypes.size() == 1)
         {
            if (getAnnotatedItem().isAnnotationPresent(scopeTypes.iterator().next().annotationType()))
            {
               this.scopeType = scopeTypes.iterator().next().annotationType();
               log.trace("Scope " + scopeType + " specified by annotation");
            }
            break;
         }
         else if (scopeTypes.size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified");
         }
      }

      if (this.scopeType == null)
      {
         initScopeTypeFromStereotype();
      }

      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace("Using default @Dependent scope");
      }
   }

   @Override
   protected void initDeploymentType()
   {
      for (AnnotatedClass<?> clazz = getAnnotatedItem(); clazz != null; clazz = clazz.getSuperclass())
      {
         Set<Annotation> deploymentTypes = clazz.getDeclaredMetaAnnotations(DeploymentType.class);
         if (deploymentTypes.size() == 1)
         {
            if (getAnnotatedItem().isAnnotationPresent(deploymentTypes.iterator().next().annotationType()))
            {
               this.deploymentType = deploymentTypes.iterator().next().annotationType();
               log.trace("Deployment type " + deploymentType + " specified by annotation");
            }
            break;
         }
         else if (deploymentTypes.size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified");
         }
      }

      if (this.deploymentType == null)
      {
         initDeploymentTypeFromStereotype();
      }

      if (this.deploymentType == null)
      {
         this.deploymentType = getDefaultDeploymentType();
         log.trace("Using default @Production deployment type");
         return;
      }
   }

   /**
    * Validate that the scope type is allowed by the stereotypes on the bean and
    * the bean type
    */
   protected void checkScopeAllowed()
   {
      log.trace("Checking if " + getScopeType() + " is allowed for " + type);
      if (getMergedStereotypes().getSupportedScopes().size() > 0)
      {
         if (!getMergedStereotypes().getSupportedScopes().contains(getScopeType()))
         {
            throw new DefinitionException("Scope " + getScopeType() + " is not an allowed by the stereotype for " + type);
         }
      }
   }

   /**
    * Validates the bean implementation
    */
   protected void checkBeanImplementation()
   {
      if (Reflections.isAbstract(getType()))
      {
         throw new DefinitionException("Web Bean implementation class " + type + " cannot be declared abstract");
      }
   }
   
   @Override
   protected void preCheckSpecialization()
   {
      super.preCheckSpecialization();
      if (getAnnotatedItem().getSuperclass() == null || getAnnotatedItem().getSuperclass().getType().equals(Object.class))
      {
         throw new DefinitionException("Specializing bean must extend another bean");
      }
   }

   /**
    * Gets the annotated item
    * 
    * @return The annotated item
    */
   @Override
   protected AnnotatedClass<T> getAnnotatedItem()
   {
      return annotatedItem;
   }

   /**
    * Gets the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      String name = Strings.decapitalize(getAnnotatedItem().getSimpleName());
      log.trace("Default name of " + type + " is " + name);
      return name;
   }

   /**
    * Gets the injectable fields
    * 
    * @return The set of injectable fields
    */
   public Set<AnnotatedField<?>> getInjectableFields()
   {
      return injectableFields;
   }

   /**
    * Gets the annotated methods
    * 
    * @return The set of annotated methods
    */
   public Set<AnnotatedMethod<?>> getInitializerMethods()
   {
      return initializerMethods;
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return "AbstractClassBean " + getName();
   }

   @Override
   /*
    * Gets the default deployment type
    * 
    * @return The default deployment type
    */
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return Production.class;
   }

   public DependentInstancesStore getDependentInstancesStore()
   {
      return dependentInstancesStore;
   }

}
