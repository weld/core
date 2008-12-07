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
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Observable;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.Production;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
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

   private static final LogProvider log = Logging.getLogProvider(AbstractClassBean.class);

   private AnnotatedClass<T> annotatedItem;
   private Set<AnnotatedField<Object>> injectableFields;
   private Set<AnnotatedMethod<Object>> initializerMethods;

   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    */
   public AbstractClassBean(Class<T> type, ManagerImpl manager)
   {
      super(manager);
      this.annotatedItem = new AnnotatedClassImpl<T>(type);
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      checkRequiredTypesImplemented();
      checkScopeAllowed();
      checkBeanImplementation();
      // TODO Interceptors
      initInitializerMethods();
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
    * Returns the producer methods
    * 
    * @return A set of producer methods. An empty set is returned if there are
    *         none present
    */
   public Set<AnnotatedMethod<Object>> getProducerMethods()
   {
      return getAnnotatedItem().getAnnotatedMethods(Produces.class);
   }

   /**
    * Returns the producer fields
    * 
    * @return A set of producer fields. An empty set is returned if there are
    *         none present
    */
   public Set<AnnotatedField<Object>> getProducerFields()
   {
      return getAnnotatedItem().getAnnotatedFields(Produces.class);
   }

   /**
    * Returns @Observer annotated fields.
    * 
    * @return A set of observing fields. An empty set is returned if there are
    *         none present.
    */
   public Set<AnnotatedField<Object>> getEventFields()
   {
      return getAnnotatedItem().getAnnotatedFields(Observable.class);
   }
   
   public Set<AnnotatedMethod<Object>> getObserverMethods()
   {
      return getAnnotatedItem().getMethodsWithAnnotatedParameters(Observes.class);
   }

   /**
    * Initializes the injection points
    */
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      injectableFields = new HashSet<AnnotatedField<Object>>();
      for (AnnotatedField<Object> annotatedField : annotatedItem.getMetaAnnotatedFields(BindingType.class))
      {
         if ( !annotatedField.isAnnotationPresent(Produces.class) )
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
         super.injectionPoints.add(annotatedField);
         }
      }
   }

   /**
    * Initializes the initializer methods
    */
   protected void initInitializerMethods()
   {
      initializerMethods = new HashSet<AnnotatedMethod<Object>>();
      for (AnnotatedMethod<Object> annotatedMethod : annotatedItem.getAnnotatedMethods(Initializer.class))
      {
         if (annotatedMethod.isStatic())
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be static");
         }
         else if (annotatedMethod.getAnnotation(Produces.class) != null)
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Produces");
         }
         else if (annotatedMethod.getAnnotation(Destructor.class) != null)
         {
            throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Destructor");
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

   /**
    * Validates that the required types are implemented
    */
   protected void checkRequiredTypesImplemented()
   {
      for (Class<?> requiredType : getMergedStereotypes().getRequiredTypes())
      {
         log.trace("Checking if required type " + requiredType + " is implemented");
         if (!requiredType.isAssignableFrom(type))
         {
            throw new DefinitionException("Required type " + requiredType + " isn't implemented on " + type);
         }
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

   /**
    * Returns the annotated item
    * 
    * @return The annotated item
    */
   @Override
   protected AnnotatedClass<T> getAnnotatedItem()
   {
      return annotatedItem;
   }

   /**
    * Returns the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      String name = Strings.decapitalize(getType().getSimpleName());
      log.trace("Default name of " + type + " is " + name);
      return name;
   }

   /**
    * Returns the injectable fields
    * 
    * @return The set of injectable fields
    */
   public Set<AnnotatedField<Object>> getInjectableFields()
   {
      return injectableFields;
   }

   /**
    * Returns the annotated methods
    * 
    * @return The set of annotated methods
    */
   public Set<AnnotatedMethod<Object>> getInitializerMethods()
   {
      return initializerMethods;
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("AbstractClassBean:\n");
      buffer.append(super.toString() + "\n");
      buffer.append("Annotated item: " + annotatedItem.toString() + "\n");
      buffer.append(Strings.collectionToString("Initializer methods: ", getInitializerMethods()));
      buffer.append(Strings.collectionToString("Injectable fields: ", getInjectableFields()));
      buffer.append(Strings.collectionToString("Producer methods: ", getProducerMethods()));
      return buffer.toString();
   }
   
   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return Production.class;
   }
}
