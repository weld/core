package org.jboss.webbeans.test.unit.activities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observer;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.bean.ForwardingBean;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;


/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class ActivitiesTest extends AbstractWebBeansTest
{

   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>();

   static
   {
      DEFAULT_BINDINGS.add(new CurrentLiteral());
   }

   private Bean<?> createDummyBean(BeanManager beanManager, final Type injectionPointType)
   {
      final Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();
      final Set<Type> types = new HashSet<Type>();
      final Set<Annotation> bindings = new HashSet<Annotation>();
      bindings.add(new AnnotationLiteral<Tame>() {});
      types.add(Object.class);
      final Bean<?> bean = new Bean<Object>()
      {

         public Set<Annotation> getBindings()
         {
            return bindings;
         }

         public Class<? extends Annotation> getDeploymentType()
         {
            return Production.class;
         }

         public Set<InjectionPoint> getInjectionPoints()
         {
            return injectionPoints;
         }

         public String getName()
         {
            return null;
         }

         public Class<? extends Annotation> getScopeType()
         {
            return Dependent.class;
         }

         public Set<Type> getTypes()
         {
            return types;
         }

         public boolean isNullable()
         {
            return false;
         }

         public boolean isSerializable()
         {
            return false;
         }

         public Object create(CreationalContext<Object> creationalContext)
         {
            return null;
         }

         public void destroy(Object instance, CreationalContext<Object> creationalContext)
         {

         }

      };
      InjectionPoint injectionPoint = new InjectionPoint()
      {

         public Bean<?> getBean()
         {
            return bean;
         }

         public Set<Annotation> getBindings()
         {
            return DEFAULT_BINDINGS;
         }

         public Member getMember()
         {
            return null;
         }

         public Type getType()
         {
            return injectionPointType;
         }

         public Annotated getAnnotated()
         {
            return null;
         }

         public boolean isDelegate()
         {
            return false;
         }

         public boolean isTransient()
         {
            return false;
         }

      };
      injectionPoints.add(injectionPoint);
      return bean;
   }

   private static class DummyContext implements Context
   {

      public <T> T get(Contextual<T> contextual)
      {
         return null;
      }

      public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
      {
         return null;
      }

      public Class<? extends Annotation> getScopeType()
      {
         return Dummy.class;
      }

      public boolean isActive()
      {
         return false;
      }

   }

   @Test
   public void testBeanBelongingToParentActivityBelongsToChildActivity()
   {
      assert getBeans(Cow.class).size() == 1;
      Contextual<?> bean = getBeans(Cow.class).iterator().next();
      BeanManager childActivity = getCurrentManager().createActivity();
      assert childActivity.getBeans(Cow.class).size() == 1;
      assert childActivity.getBeans(Cow.class).iterator().next().equals(bean);
   }

   @Test
   public void testBeanBelongingToParentActivityCanBeInjectedIntoChildActivityBean()
   {
      assert getBeans(Cow.class).size() == 1;
      Contextual<?> bean = getBeans(Cow.class).iterator().next();
      BeanManager childActivity = getCurrentManager().createActivity();
      Bean<?> dummyBean = createDummyBean(childActivity, Cow.class);
      childActivity.addBean(dummyBean);
      assert childActivity.getInjectableReference(dummyBean.getInjectionPoints().iterator().next(), childActivity.createCreationalContext(dummyBean)) != null;
   }

   @Test
   public void testObserverBelongingToParentActivityBelongsToChildActivity()
   {
      assert getCurrentManager().resolveObservers(new NightTime()).size() == 1;
      Observer<?> observer = getCurrentManager().resolveObservers(new NightTime()).iterator().next();
      BeanManager childActivity = getCurrentManager().createActivity();
      assert childActivity.resolveObservers(new NightTime()).size() == 1;
      assert childActivity.resolveObservers(new NightTime()).iterator().next().equals(observer);
   }

   @Test
   public void testObserverBelongingToParentFiresForChildActivity()
   {
      Fox.setObserved(false);
      BeanManager childActivity = getCurrentManager().createActivity();
      childActivity.fireEvent(new NightTime());
      assert Fox.isObserved();
   }

   @Test
   public void testContextObjectBelongingToParentBelongsToChild()
   {
      Context context = new DummyContext()
      {

         @Override
         public boolean isActive()
         {
            return true;
         }

      };
      getCurrentManager().addContext(context);
      BeanManager childActivity = getCurrentManager().createActivity();
      assert childActivity.getContext(Dummy.class) != null;
   }

   @Test
   public void testBeanBelongingToChildActivityCannotBeInjectedIntoParentActivityBean()
   {
      assert getBeans(Cow.class).size() == 1;
      BeanManager childActivity = getCurrentManager().createActivity();
      Bean<?> dummyBean = createDummyBean(childActivity, Cow.class);
      childActivity.addBean(dummyBean);
      assert getBeans(Object.class, new AnnotationLiteral<Tame>() {}).size() == 0;
   }

   @Test(expectedExceptions=UnsatisfiedResolutionException.class)
   public void testInstanceProcessedByParentActivity()
   {
      Context dummyContext = new DummyContext();
      getCurrentManager().addContext(dummyContext);
      assert getBeans(Cow.class).size() == 1;
      final Bean<Cow> bean = getBeans(Cow.class).iterator().next();
      BeanManager childActivity = getCurrentManager().createActivity();
      final Set<Annotation> bindingTypes = new HashSet<Annotation>();
      bindingTypes.add(new AnnotationLiteral<Tame>() {});
      childActivity.addBean(new ForwardingBean<Cow>()
            {

         @Override
         protected Bean<Cow> delegate()
         {
            return bean;
         }

         @Override
         public Set<Annotation> getBindings()
         {
            return bindingTypes;
         }

            });
      createContextualInstance(Field.class).get();
   }

   @Test
   public void testObserverBelongingToChildDoesNotFireForParentActivity()
   {
      BeanManager childActivity = getCurrentManager().createActivity();
      Observer<NightTime> observer = new Observer<NightTime>()
      {

         public boolean notify(NightTime event)
         {
            assert false;
            return false;
         }

      };
      childActivity.addObserver(observer);
      getCurrentManager().fireEvent(new NightTime());
   }

}
