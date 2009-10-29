package org.jboss.weld.test.unit.activities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.ForwardingBean;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;


/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class ActivitiesTest extends AbstractWeldTest
{

   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>();

   static
   {
      DEFAULT_BINDINGS.add(new DefaultLiteral());
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

         public Set<Annotation> getQualifiers()
         {
            return bindings;
         }

         public Set<InjectionPoint> getInjectionPoints()
         {
            return injectionPoints;
         }

         public String getName()
         {
            return null;
         }

         public Class<? extends Annotation> getScope()
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

         public Object create(CreationalContext<Object> creationalContext)
         {
            return null;
         }

         public void destroy(Object instance, CreationalContext<Object> creationalContext)
         {

         }

         public Class<?> getBeanClass()
         {
            return Object.class;
         }

         public boolean isAlternative()
         {
            return false;
         }

         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

      };
      InjectionPoint injectionPoint = new InjectionPoint()
      {

         public Bean<?> getBean()
         {
            return bean;
         }

         public Set<Annotation> getQualifiers()
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

      public Class<? extends Annotation> getScope()
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
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
      Bean<?> dummyBean = createDummyBean(childActivity, Cow.class);
      childActivity.addBean(dummyBean);
      assert childActivity.getInjectableReference(dummyBean.getInjectionPoints().iterator().next(), childActivity.createCreationalContext(dummyBean)) != null;
   }

//   @Test
//   public void testObserverBelongingToParentActivityBelongsToChildActivity()
//   {
//      assert getCurrentManager().resolveObservers(new NightTime()).size() == 1;
//      Observer<?> observer = getCurrentManager().resolveObservers(new NightTime()).iterator().next();
//      BeanManager childActivity = getCurrentManager().createActivity();
//      assert childActivity.resolveObservers(new NightTime()).size() == 1;
//      assert childActivity.resolveObservers(new NightTime()).iterator().next().equals(observer);
//   }

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
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
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
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
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
         public Set<Annotation> getQualifiers()
         {
            return bindingTypes;
         }

         @Override
         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

            });
      createContextualInstance(Field.class).get();
   }

   @Test
   public void testObserverBelongingToChildDoesNotFireForParentActivity()
   {
      
//      BeanManager childActivity = getCurrentManager().createActivity();
//      ObserverMethod<NightTime> observer = new Observer<NightTime>()
//      {
//
//         public boolean notify(NightTime event)
//         {
//            assert false;
//            return false;
//         }
//
//      };
//      //TODO Fix this test to use an observer method in a child activity
////      childActivity.addObserver(observer);
//      getCurrentManager().fireEvent(new NightTime());
   }

}
