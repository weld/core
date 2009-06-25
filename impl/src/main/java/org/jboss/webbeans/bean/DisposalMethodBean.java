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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.ScopeType;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.deployment.DeploymentType;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

public class DisposalMethodBean<T> extends AbstractBean<T, Method>
{

   private static final LogProvider log = Logging.getLogProvider(AbstractProducerBean.class);
   protected AbstractClassBean<?> declaringBean;
   private DisposalMethodBean<?> specializedBean;
   protected MethodInjectionPoint<T> disposalMethodInjectionPoint;
   protected Set<WBInjectionPoint<?, ?>> disposalInjectionPoints;
   private final String id;

   protected DisposalMethodBean(BeanManagerImpl manager, WBMethod<T> disposalMethod, AbstractClassBean<?> declaringBean)
   {
      super(manager);
      this.disposalMethodInjectionPoint = MethodInjectionPoint.of(this, disposalMethod);
      this.declaringBean = declaringBean;
      checkDisposalMethod();
      initBindings();
      initType();
      initTypes();
      this.id = createId("DisposalMethod-" + declaringBean.getName() + "-" + disposalMethod.getSignature().toString());

   }

   @SuppressWarnings("unchecked")
   protected void initType()
   {
      this.type = (Class<T>) disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getJavaClass();
   }

   @Override
   public WBMethod<T> getAnnotatedItem()
   {
      return disposalMethodInjectionPoint;
   }

   public static <T> DisposalMethodBean<T> of(BeanManagerImpl manager, WBMethod<T> disposalMethod, AbstractClassBean<?> declaringBean)
   {
      return new DisposalMethodBean<T>(manager, disposalMethod, declaringBean);
   }

   protected void initInjectionPoints()
   {
      disposalInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();

      List<? extends WBParameter<?>> disposalMethodParameters = disposalMethodInjectionPoint.getParameters();

      // First one must be @Disposes, if more, register injectionpoints
      if (disposalMethodParameters.size() > 1)
      {
         for (int i = 1; i < disposalMethodParameters.size(); i++)
         {
            WBParameter<?> parameter = disposalMethodParameters.get(i);
            disposalInjectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
         }
      }

      injectionPoints.add(MethodInjectionPoint.of(declaringBean, disposalMethodInjectionPoint));

   }

   @Override
   protected void initBindings()
   {
      // At least 1 parameter exists, already checked in constructor
      this.bindings = new HashSet<Annotation>();
      this.bindings.addAll(disposalMethodInjectionPoint.getParameters().get(0).getBindings());
      initDefaultBindings();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return declaringBean.getDeploymentType();
   }

   /**
    * Initializes the API types
    */
   @Override
   protected void initTypes()
   {
      Set<Type> types = new HashSet<Type>();
      types = new HashSet<Type>();
      types.addAll(disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getTypeClosure());
      types.add(getType());
      types.add(Object.class);
      super.types = types;
   }

   @Override
   public Set<WBInjectionPoint<?, ?>> getAnnotatedInjectionPoints()
   {
      return injectionPoints;
   }

   @Override
   public String getName()
   {
      return disposalMethodInjectionPoint.getPropertyName();
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return declaringBean.getScopeType();
   }

   @Override
   public Set<Type> getTypes()
   {
      return types;
   }

   @Override
   public String toString()
   {
      return disposalMethodInjectionPoint.toString();
   }

   @Override
   public boolean isNullable()
   {
      return false;
   }

   @Override
   public boolean isSerializable()
   {
      return declaringBean.isSerializable();
   }

   @Override
   public boolean isProxyable()
   {
      return true;
   }

   public T create(CreationalContext<T> creationalContext)
   {
      return null;
   }

   public void invokeDisposeMethod(Object instance, CreationalContext<?> creationalContext)
   {
      // TODO WTF - why isn't this using getReceiver!? Why is it assigning the beanInstance as the beanObject!?!
      Object beanInstance = disposalMethodInjectionPoint.isStatic() ? declaringBean : getManager().getReference(declaringBean, declaringBean.getType(), creationalContext);
      disposalMethodInjectionPoint.invokeWithSpecialValue(beanInstance, Disposes.class, instance, manager, creationalContext, IllegalArgumentException.class);
   }

   private void checkDisposalMethod()
   {
      if (!disposalMethodInjectionPoint.getParameters().get(0).isAnnotationPresent(Disposes.class))
      {
         throw new DefinitionException(disposalMethodInjectionPoint.toString() + " doesn't have @Dispose as first parameter");
      }
      if (disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).size() > 1)
      {
         throw new DefinitionException(disposalMethodInjectionPoint.toString() + " has more than one @Dispose parameters");
      }
      if (disposalMethodInjectionPoint.getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("@Observes is not allowed on disposal method, see " + disposalMethodInjectionPoint.toString());
      }
      if (disposalMethodInjectionPoint.getAnnotation(Initializer.class) != null)
      {
         throw new DefinitionException("@Intitializer is not allowed on a disposal method, see " + disposalMethodInjectionPoint.toString());
      }
      if (disposalMethodInjectionPoint.getAnnotation(Produces.class) != null)
      {
         throw new DefinitionException("@Produces is not allowed on a disposal method, see " + disposalMethodInjectionPoint.toString());
      }
      if (declaringBean instanceof EnterpriseBean)
      {
         boolean methodDeclaredOnTypes = false;
         // TODO use annotated item?
         for (Type type : declaringBean.getTypes())
         {
            if (type instanceof Class)
            {
               Class<?> clazz = (Class<?>) type;
               try
               {
                  clazz.getDeclaredMethod(disposalMethodInjectionPoint.getName(), disposalMethodInjectionPoint.getParameterTypesAsArray());
                  methodDeclaredOnTypes = true;
               }
               catch (NoSuchMethodException nsme)
               {
                  // No - op
               }
            }
         }
         if (!methodDeclaredOnTypes)
         {
            throw new DefinitionException("Producer method " + toString() + " must be declared on a business interface of " + declaringBean);
         }
      }
   }

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      if (declaringBean.getAnnotatedItem().getSuperclass().getDeclaredMethod(getAnnotatedItem().getAnnotatedMethod()) == null)
      {
         throw new DefinitionException("Specialized disposal method does not override a method on the direct superclass");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      WBMethod<?> superClassMethod = declaringBean.getAnnotatedItem().getSuperclass().getMethod(getAnnotatedItem().getAnnotatedMethod());
      if (environment.getProducerMethod(superClassMethod) == null)
      {
         throw new IllegalStateException(toString() + " does not specialize a bean");
      }
      this.specializedBean = environment.getDisposalMethod(superClassMethod);
   }

   @Override
   public Class<T> getType()
   {
      return type;
   }

   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return declaringBean.getDeploymentType();
   }

   @Override
   protected String getDefaultName()
   {
      return disposalMethodInjectionPoint.getPropertyName();
   }

   @Override
   protected void initDeploymentType()
   {
      Set<Annotation> deploymentTypes = getAnnotatedItem().getMetaAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new DefinitionException("At most one deployment type may be specified (" + deploymentTypes + " are specified) on " + getAnnotatedItem().toString());
      }
      else if (deploymentTypes.size() == 1)
      {
         this.deploymentType = deploymentTypes.iterator().next().annotationType();
         log.trace("Deployment type " + deploymentType + " specified by annotation");
         return;
      }

      initDeploymentTypeFromStereotype();

      if (this.deploymentType == null)
      {
         this.deploymentType = getDefaultDeploymentType();
         log.trace("Using default " + this.deploymentType + " deployment type");
         return;
      }
   }

   @Override
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

      initScopeTypeFromStereotype();

      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace("Using default @Dependent scope");
      }      
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      // No-op. Producer method dependent objects are destroyed in producer method bean  
   }

   @Override
   public String getId()
   {
      return id;
   }

}
