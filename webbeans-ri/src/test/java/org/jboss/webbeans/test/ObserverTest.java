package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.bindings.AsynchronousAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.test.mock.MockObserverImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the implementation of Observer.
 * 
 * @author David Allen
 * 
 */
@SpecVersion("20081024-PDR")
public class ObserverTest
{
   private ManagerImpl manager;
   private SimpleBeanModel<Tuna> tuna;
   private InjectableMethod<?> om;

   public class SampleEvent
   {
      // Simple class used for testing
   }

   public class AnObserver
   {
      protected boolean notified = false;

      public void observe(@Observes @Asynchronous SampleEvent e)
      {
         // An observer method
         this.notified = true;
      }
   }

   @BeforeMethod
   public void before() throws Exception
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      manager = new MockManagerImpl(enabledDeploymentTypes);

      // Create an observer with known binding types
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedType<Tuna> annotatedItem = new SimpleAnnotatedType<Tuna>(Tuna.class, annotations);
      tuna = new SimpleBeanModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), annotatedItem, manager);
      om = new InjectableMethod<Object>(AnObserver.class.getMethod("observe", new Class[] { SampleEvent.class }));
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.ObserverImpl#notify(javax.webbeans.Container, java.lang.Object)}
    * .
    */
   @Test(groups = "observerMethod") @SpecAssertion(section={"7.5.7"})
   public final void testNotify() throws Exception
   {
      AnObserver observerInstance = new AnObserver();
      Observer<SampleEvent> observer = new MockObserverImpl<SampleEvent>(tuna, om, SampleEvent.class);
      ((MockObserverImpl<SampleEvent>) observer).setInstance(observerInstance);
      SampleEvent event = new SampleEvent();
      observer.notify(event);
      assert observerInstance.notified;
   }

}
