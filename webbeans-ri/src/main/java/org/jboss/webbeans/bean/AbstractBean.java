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
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.ScopeType;
import javax.webbeans.Specializes;
import javax.webbeans.Standard;
import javax.webbeans.Stereotype;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.ejb.DefaultEnterpriseBeanLookup;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.jlr.AbstractAnnotatedItem.AnnotationMap;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
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
   private static Set<Class<?>> STANDARD_WEB_BEAN_CLASSES = new HashSet<Class<?>>(Arrays.asList(DefaultEnterpriseBeanLookup.class));

   /**
    * Helper class for getting deployment type
    * 
    * Loops through the enabled deployment types (backwards) and returns the first one
    * present in the possible deployments type, resulting in the deployment type of 
    * highest priority
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

   // Reference to WBRI manager
   private ManagerImpl manager;
   private Set<Annotation> bindingTypes;
   protected String name;
   protected Class<? extends Annotation> scopeType;
   private MergedStereotypes<T, E> mergedStereotypes;
   protected Class<? extends Annotation> deploymentType;
   protected Class<T> type;
   protected AnnotatedMethod<Object> removeMethod;
   protected Set<Class<?>> apiTypes;
   protected Set<AnnotatedItem<?, ?>> injectionPoints;

   private boolean primitive;

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
   }

   /**
    * Initializes the bean and its metadata
    */
   protected void init()
   {
      mergedStereotypes = new MergedStereotypes<T, E>(getAnnotatedItem().getMetaAnnotations(Stereotype.class), manager);
      initType();
      initPrimitive();
      log.debug("Building Web Bean bean metadata for " + getType());
      initBindingTypes();
      initName();
      initDeploymentType();
      checkDeploymentType();
      initScopeType();
      initApiTypes();
   }

   /**
    * Initializes the API types
    */
   protected void initApiTypes()
   {
      apiTypes = Reflections.getTypeHierachy(getType());
   }

   /**
    * Initializes the binding types
    */
   protected void initBindingTypes()
   {

      this.bindingTypes = new HashSet<Annotation>();
      if (isDefinedInXml())
      {
         boolean xmlSpecialization = false;
         Set<Annotation> xmlBindingTypes = null;
         this.bindingTypes.addAll(xmlBindingTypes);
         if (xmlSpecialization)
         {
            this.bindingTypes.addAll(bindingTypes);
            log.trace("Using binding types " + this.bindingTypes + " specified in XML and specialized type");
         }
         else
         {
            log.trace("Using binding types " + this.bindingTypes + " specified in XML");
         }
         return;
      }
      else if (!mergedStereotypes.isDeclaredInXml())
      {
         boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
         this.bindingTypes.addAll(getAnnotatedItem().getMetaAnnotations(BindingType.class));
         if (specialization)
         {
            this.bindingTypes.addAll(getSpecializedType().getBindingTypes());
            log.trace("Using binding types " + bindingTypes + " specified by annotations and specialized supertype");
         }
         else if (bindingTypes.size() == 0)
         {
            log.trace("Adding default @Current binding type");
            this.bindingTypes.add(new CurrentAnnotationLiteral());
         }
         else
         {
            log.trace("Using binding types " + bindingTypes + " specified by annotations");
         }
         return;
      }
   }

   /**
    * Initializes the deployment types
    */
   @SuppressWarnings("null")
   protected void initDeploymentType()
   {
      if (isDefinedInXml())
      {
         Set<Annotation> xmlDeploymentTypes = null;
         if (xmlDeploymentTypes.size() > 1)
         {
            throw new DefinitionException ("At most one deployment type may be specified (" + xmlDeploymentTypes + " are specified)");
         }

         if (xmlDeploymentTypes.size() == 1)
         {
            this.deploymentType = xmlDeploymentTypes.iterator().next().annotationType();
            log.trace("Deployment type " + deploymentType + " specified in XML");
            return;
         }
      }
      else
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

         if (getMergedStereotypes().getPossibleDeploymentTypes().size() > 0)
         {
            this.deploymentType = getDeploymentType(manager.getEnabledDeploymentTypes(), getMergedStereotypes().getPossibleDeploymentTypes());
            log.trace("Deployment type " + deploymentType + " specified by stereotype");
            return;
         }
      }

      this.deploymentType = Production.class;
      log.trace("Using default @Production deployment type");
      return;
   }

   /**
    * Initializes the injection points
    */
   protected void initInjectionPoints()
   {
      injectionPoints = new HashSet<AnnotatedItem<?, ?>>();
      if (removeMethod != null)
      {
         for (AnnotatedParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }

   /**
    * Initializes the name
    */
   protected void initName()
   {
      boolean beanNameDefaulted = false;
      if (isDefinedInXml())
      {
         boolean xmlSpecialization = false;
         if (xmlSpecialization)
         {
            throw new DefinitionException("Name specified for specialized bean (declared in XML)");
         }
         String xmlName = "";
         if ("".equals(xmlName))
         {
            log.trace("Using default name (specified in XML)");
            beanNameDefaulted = true;
         }
         else
         {
            log.trace("Using name " + xmlName + " specified in XML");
            this.name = xmlName;
            return;
         }
      }
      else
      {
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

   /**
    * Initializes the scope type
    */
   @SuppressWarnings("null")
   protected void initScopeType()
   {
      if (isDefinedInXml())
      {
         Set<Class<? extends Annotation>> scopeTypes = null;
         if (scopeTypes.size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified in XML");
         }

         if (scopeTypes.size() == 1)
         {
            this.scopeType = scopeTypes.iterator().next();
            log.trace("Scope " + scopeType + " specified in XML");
            return;
         }
      }
      else
      {
         if (getAnnotatedItem().getMetaAnnotations(ScopeType.class).size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified");
         }

         if (getAnnotatedItem().getMetaAnnotations(ScopeType.class).size() == 1)
         {
            this.scopeType = getAnnotatedItem().getMetaAnnotations(ScopeType.class).iterator().next().annotationType();
            log.trace("Scope " + scopeType + " specified by annotation");
            return;
         }
      }

      if (getMergedStereotypes().getPossibleScopeTypes().size() == 1)
      {
         this.scopeType = getMergedStereotypes().getPossibleScopeTypes().iterator().next().annotationType();
         log.trace("Scope " + scopeType + " specified by stereotype");
         return;
      }
      else if (getMergedStereotypes().getPossibleScopeTypes().size() > 1)
      {
         throw new DefinitionException ("All stereotypes must specify the same scope OR a scope must be specified on the bean");
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
         throw new DefinitionException ("type: " + getType() + " must specify a deployment type");
      }
      else if (deploymentType.equals(Standard.class) && !STANDARD_WEB_BEAN_CLASSES.contains(getAnnotatedItem().getType()))
      {
         throw new DefinitionException(getAnnotatedItem() + " cannot have deployment type @Standard");
      }
   }

   /**
    * Destroys a bean instance
    * 
    * @param instance The instance to destroy
    * 
    * @see javax.webbeans.manager.Bean#destroy(Object)
    */
   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
   }

   /**
    * Binds the decorators to the proxy
    */
   protected void bindDecorators()
   {
      // TODO
   }

   /**
    * Binds the interceptors to the proxy
    */
   protected void bindInterceptors()
   {
      // TODO
   }

   /**
    * Returns the annotated time the bean reresents
    * 
    * @return The annotated item
    */
   protected abstract AnnotatedItem<T, E> getAnnotatedItem();

   /**
    * Returns the binding types
    * 
    * @return The set of binding types
    * 
    * @see javax.webbeans.manager.Bean#getBindingTypes()
    */
   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   /**
    * Returns the declared bean type
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
    * Returns the default name of the bean
    * 
    * @return The default name
    */
   protected abstract String getDefaultName();

   /**
    * Returns the deployment type of the bean
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
    * Returns the injection points of the bean
    * 
    * @return The set of injection points
    */
   public Set<AnnotatedItem<?, ?>> getInjectionPoints()
   {
      return injectionPoints;
   }

   /**
    * Returns the Web Beans manager reference
    * 
    * @return The manager
    */
   @Override
   protected ManagerImpl getManager()
   {
      return manager;
   }

   /**
    * Returns the merged sterotypes of the bean
    * 
    * @return The set of merged stereotypes
    */
   public MergedStereotypes<T, E> getMergedStereotypes()
   {
      return mergedStereotypes;
   }

   /**
    * Returns the name of the bean
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
    * Returns the remove method of the bean
    * 
    * @return The remove method
    */
   public AnnotatedMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

   /**
    * Returns the scope type of the bean
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
    * Returns the specializes type of the bean
    * 
    * @return The specialized type
    */
   protected AbstractBean<? extends T, E> getSpecializedType()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns the type of the bean
    * 
    * @return The type
    */
   public Class<T> getType()
   {
      return type;
   }

   /**
    * Returns the API types of the bean
    * 
    * @return The set of API types
    * 
    * @see javax.webbeans.manager.Bean#getTypes()
    */
   @Override
   public Set<Class<?>> getTypes()
   {
      return apiTypes;
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
    * Indicates if bean was defined in XML
    * 
    * @return True if defined in XML, false if defined with annotations
    */
   protected boolean isDefinedInXml()
   {
      return false;
   }

   /**
    * Inicates if bean is nullable
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

   /**
    * Indicates if bean is serializable
    * 
    * @return True if serializable, false otherwise
    * 
    * @see @see javax.webbeans.manager.Bean#isSerializable()
    */
   @Override
   public boolean isSerializable()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("AbstractBean:\n");
      buffer.append("Name: " + name + "\n");
      buffer.append("Type: " + type + "\n");
      buffer.append("Scope type " + scopeType.toString() + "\n");
      buffer.append("Deployment type: " + deploymentType.toString() + "\n");
      buffer.append("Primitive : " + primitive + "\n");
      buffer.append("Declared bean type: " + (declaredBeanType == null ? "null" : declaredBeanType.toString()) + "\n");
      buffer.append("Remove method: " + (removeMethod == null ? "null" : removeMethod.toString()) + "\n");
      buffer.append("Binding types: " + bindingTypes.size() + "\n");
      int i = 0;
      for (Annotation bindingType : bindingTypes)
      {
         buffer.append(++i + " - " + bindingType.toString() + "\n");
      }
      buffer.append("API types: " + apiTypes.size() + "\n");
      i = 0;
      for (Class<?> apiType : apiTypes)
      {
         buffer.append(++i + " - " + apiType.getName() + "\n");
      }
      buffer.append(mergedStereotypes.toString() + "\n");
      buffer.append("Injection points: " + injectionPoints.size() + "\n");
      i = 0;
      for (AnnotatedItem<?, ?> injectionPoint : injectionPoints)
      {
         buffer.append(++i + " - " + injectionPoint.toString() + "\n");
      }
      return buffer.toString();
   }
}
