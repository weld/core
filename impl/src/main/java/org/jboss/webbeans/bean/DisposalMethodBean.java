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
import javax.inject.manager.Bean;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.RootManager;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class DisposalMethodBean<T> extends AbstractBean<T, Method>
{

   protected DisposalMethodBean(RootManager manager, AnnotatedMethod<T> disposalMethod, Bean<?> declaringBean)
   {
      super(manager);
      this.disposalMethod = disposalMethod;
      this.declaringBean = declaringBean;
      checkDisposalMethod();
      initInjectionPoints();
      initType();
      initTypes();
      this.id = createId("DisposalMethod-" + declaringBean.getName() + "-"+ disposalMethod.getSignature().toString());
      
   }

   protected Bean<?> declaringBean;
   protected AnnotatedMethod<T> disposalMethod;
   protected Set<AnnotatedInjectionPoint<?, ?>> disposalInjectionPoints;
   private String id;

   @Override
   protected void initTypes()
   {
      Set<Type> types = new HashSet<Type>();
      types = new HashSet<Type>();
      types.add(getType());
      types.add(Object.class); // FODO: Maybe not?
      super.types = types;
   }

   protected void initType()
   {
      this.type = (Class<T>) disposalMethod.getAnnotatedParameters(Disposes.class).get(0).getRawType();
   }

   public static <T> DisposalMethodBean<T> of(RootManager manager, AnnotatedMethod<T> disposalMethod, Bean<?> declaringBean)
   {
      return new DisposalMethodBean<T>(manager, disposalMethod, declaringBean);
   }

   private void initInjectionPoints()
   {
      disposalInjectionPoints = new HashSet<AnnotatedInjectionPoint<?, ?>>();

      List<? extends AnnotatedParameter<?>> disposalMethodParameters = disposalMethod.getParameters();

      // First one must be @Disposes, if more, register injectionpoints
      if (disposalMethodParameters.size() > 1)
      {
         for (int i = 1; i < disposalMethodParameters.size(); i++)
         {
            AnnotatedParameter<?> parameter = disposalMethodParameters.get(i);
            disposalInjectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
         }
      }

      injectionPoints.add(MethodInjectionPoint.of(declaringBean, disposalMethod));

   }

   @Override
   public Set<Annotation> getBindings()
   {
      // At least 1 parameter exists, already checked in constructor
      return disposalMethod.getParameters().get(0).getBindings();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return declaringBean.getDeploymentType();
   }

   public Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints()
   {
      return injectionPoints;
   }

   @Override
   public String getName()
   {
      return disposalMethod.getPropertyName();
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
      return disposalMethod.toString();
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

      Object beanInstance = disposalMethod.isStatic() ? declaringBean : getManager().getInstance(declaringBean);

      try
      {
         disposalMethod.invoke(beanInstance, parameters.toArray());
      }
      catch (Exception e)
      {
         // TODO: 
      }

   }

   private void checkDisposalMethod()
   {
      if (!disposalMethod.getParameters().get(0).isAnnotationPresent(Disposes.class))
      {
         throw new DefinitionException(disposalMethod.toString() + " doesn't have @Dispose as first parameter");
      }
      if (disposalMethod.getAnnotatedParameters(Disposes.class).size() > 1)
      {
         throw new DefinitionException(disposalMethod.toString() + " has more than one @Dispose parameters");
      }
      if (disposalMethod.getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("@Observes is not allowed on disposal method, see " + disposalMethod.toString());
      }
      if (disposalMethod.getAnnotation(Initializer.class) != null)
      {
         throw new DefinitionException("@Intitializer is not allowed on a disposal method, see " + disposalMethod.toString());
      }
      if (disposalMethod.getAnnotation(Produces.class) != null)
      {
         throw new DefinitionException("@Produces is not allowed on a disposal method, see " + disposalMethod.toString());
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
                  clazz.getDeclaredMethod(disposalMethod.getName(), disposalMethod.getParameterTypesAsArray());
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
   protected AnnotatedItem<T, Method> getAnnotatedItem()
   {
      return disposalMethod;
   }

   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return declaringBean.getDeploymentType();
   }

   @Override
   protected String getDefaultName()
   {
      return disposalMethod.getPropertyName();
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
