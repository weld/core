package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.context.CreationalContext;
import javax.event.Observes;
import javax.inject.DefinitionException;
import javax.inject.Disposes;
import javax.inject.Initializer;
import javax.inject.Produces;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class DisposalMethodBean<T> extends AbstractBean<T, Method>
{

   protected DisposalMethodBean(ManagerImpl manager, AnnotatedMethod<T> disposalMethod, AbstractClassBean<?> declaringBean)
   {
      super(manager);
      this.disposalMethodInjectionPoint = MethodInjectionPoint.of(this, disposalMethod);
      this.declaringBean = declaringBean;
      checkDisposalMethod();
      initInjectionPoints();
      initType();
      initTypes();
      this.id = createId("DisposalMethod-" + declaringBean.getName() + "-" + disposalMethod.getSignature().toString());

   }

   protected AbstractClassBean<?> declaringBean;
   private ProducerMethodBean<?> specializedBean;
   protected MethodInjectionPoint<T> disposalMethodInjectionPoint;
   protected Set<AnnotatedInjectionPoint<?, ?>> disposalInjectionPoints;
   private String id;

   protected void initType()
   {
      this.type = (Class<T>) disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getRawType();
   }

   public AnnotatedMethod<T> getAnnotatedItem()
   {
      return disposalMethodInjectionPoint;
   }

   public static <T> DisposalMethodBean<T> of(ManagerImpl manager, AnnotatedMethod<T> disposalMethod, AbstractClassBean<?> declaringBean)
   {
      return new DisposalMethodBean<T>(manager, disposalMethod, declaringBean);
   }

   private void initInjectionPoints()
   {
      disposalInjectionPoints = new HashSet<AnnotatedInjectionPoint<?, ?>>();

      List<? extends AnnotatedParameter<?>> disposalMethodParameters = disposalMethodInjectionPoint.getParameters();

      // First one must be @Disposes, if more, register injectionpoints
      if (disposalMethodParameters.size() > 1)
      {
         for (int i = 1; i < disposalMethodParameters.size(); i++)
         {
            AnnotatedParameter<?> parameter = disposalMethodParameters.get(i);
            disposalInjectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
         }
      }

      injectionPoints.add(MethodInjectionPoint.of(declaringBean, disposalMethodInjectionPoint));

   }

   @Override
   public Set<Annotation> getBindings()
   {
      // At least 1 parameter exists, already checked in constructor
      return disposalMethodInjectionPoint.getParameters().get(0).getBindings();
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
      types.addAll(disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getFlattenedTypeHierarchy());
      types.add(getType());
      types.add(Object.class);
      super.types = types;
   }

   public Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints()
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
   public Set<? extends Type> getTypes()
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

   public T create(CreationalContext<T> creationalContext)
   {
      return null;
   }

   public void invokeDisposeMethod(Object instance)
   {

      List<Object> parameters = new LinkedList<Object>();

      parameters.add(instance);

      for (InjectionPoint injectionPoint : disposalInjectionPoints)
      {
         Object injectionObject = getManager().getInstanceToInject(injectionPoint);
         parameters.add(injectionObject);
      }

      Object beanInstance = disposalMethodInjectionPoint.isStatic() ? declaringBean : getManager().getInstance(declaringBean);

      try
      {
         disposalMethodInjectionPoint.invoke(beanInstance, parameters.toArray());
      }
      catch (Exception e)
      {
         // TODO:
      }

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
      AnnotatedMethod<?> superClassMethod = declaringBean.getAnnotatedItem().getSuperclass().getMethod(getAnnotatedItem().getAnnotatedMethod());
      if (environment.getProducerMethod(superClassMethod) == null)
      {
         throw new IllegalStateException(toString() + " does not specialize a bean");
      }
      this.specializedBean = environment.getProducerMethod(superClassMethod);
   }

   @Override
   public Class<T> getType()
   {
      return type;
   }

   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {

   }

   @Override
   public boolean isPrimitive()
   {
      return false;
   }

   @Override
   public boolean isProxyable()
   {
      return false;
   }

   @Override
   public boolean isSpecializing()
   {
      return false;
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
   }

   @Override
   protected void initScopeType()
   {
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return null;
   }

   public void destroy(T instance)
   {

   }

   @Override
   public String getId()
   {
      return id;
   }

}
