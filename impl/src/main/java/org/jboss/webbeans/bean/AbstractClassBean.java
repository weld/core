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

import javax.context.CreationalContext;
import javax.context.Dependent;
import javax.context.ScopeType;
import javax.event.Observes;
import javax.inject.CreationException;
import javax.inject.DefinitionException;
import javax.inject.DeploymentType;
import javax.inject.Disposes;
import javax.inject.Initializer;
import javax.inject.Produces;
import javax.inject.Production;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.FieldInjectionPoint;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Beans;
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
   private Set<FieldInjectionPoint<?>> injectableFields;
   // The initializer methods
   private Set<MethodInjectionPoint<?>> initializerMethods;
   private Set<String> dependencies;
   
   private final String id;

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
      this.id = createId(getClass().getSimpleName() + "-" + type.getName());
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      super.initialize(environment);
      checkScopeAllowed();
      checkBeanImplementation();
      initInitializerMethods();
   }

   /**
    * Injects bound fields
    * 
    * @param instance The instance to inject into
    */
   protected void injectBoundFields(T instance, CreationalContext<T> creationalContext)
   {
      for (FieldInjectionPoint<?> injectableField : injectableFields)
      {
         injectableField.inject(instance, manager, creationalContext);
      }
   }
   
   /**
    * Calls all initializers of the bean
    * 
    * @param instance The bean instance
    */
   protected void callInitializers(T instance, CreationalContext<T> creationalContext)
   {
      for (MethodInjectionPoint<?> initializer : getInitializerMethods())
      {
         initializer.invoke(instance, manager, creationalContext, CreationException.class);
      }
   }

   /**
    * Initializes the bean type
    */
   protected void initType()
   {
      log.trace("Bean type specified in Java");
      this.type = getAnnotatedItem().getRawType();
      this.dependencies = new HashSet<String>();
      for (Class<?> clazz = type.getSuperclass(); clazz != Object.class; clazz = clazz.getSuperclass())
      {
         dependencies.add(clazz.getName());
      }
   }

   /**
    * Initializes the injection points
    */
   protected void initInjectionPoints()
   {
      injectableFields = new HashSet<FieldInjectionPoint<?>>(Beans.getFieldInjectionPoints(annotatedItem, this));
      super.injectionPoints.addAll(injectableFields);
      for (AnnotatedMethod<?> initializer : getInitializerMethods())
      {
         for (AnnotatedParameter<?> parameter : initializer.getParameters())
         {
            injectionPoints.add(ParameterInjectionPoint.of(this, parameter));
         }
      }
   }

   /**
    * Initializes the initializer methods
    */
   protected void initInitializerMethods()
   {
      initializerMethods = new HashSet<MethodInjectionPoint<?>>();
      for (AnnotatedMethod<?> method : annotatedItem.getAnnotatedMethods(Initializer.class))
      {
         if (method.isStatic())
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot be static on " + getAnnotatedItem());
         }
         else if (method.getAnnotation(Produces.class) != null)
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot be annotated @Produces on " + getAnnotatedItem());
         }
         else if (method.getAnnotatedParameters(Disposes.class).size() > 0)
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot have parameters annotated @Disposes on " + getAnnotatedItem());
         }
         else if (method.getAnnotatedParameters(Observes.class).size() > 0)
         {
            throw new DefinitionException("Initializer method " + method.toString() + " cannot be annotated @Observes on " + getAnnotatedItem());
         }
         else
         {
            initializerMethods.add(MethodInjectionPoint.of(this, method));
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
            throw new DefinitionException("At most one scope may be specified on " + getAnnotatedItem());
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
            throw new DefinitionException("At most one deployment type may be specified");
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
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      super.preSpecialize(environment);
      if (getAnnotatedItem().getSuperclass() == null || getAnnotatedItem().getSuperclass().getRawType().equals(Object.class))
      {
         throw new DefinitionException("Specializing bean must extend another bean " + toString());
      }
   }

   /**
    * Gets the annotated item
    * 
    * @return The annotated item
    */
   @Override
   public AnnotatedClass<T> getAnnotatedItem()
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
    * Gets the annotated methods
    * 
    * @return The set of annotated methods
    */
   public Set<? extends MethodInjectionPoint<?>> getInitializerMethods()
   {
      return initializerMethods;
   }
   
   // TODO maybe a better way to expose this?
   public Set<String> getSuperclasses()
   {
      return dependencies;
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
   
   @Override
   public String getId()
   {
      return id;
   }

}
