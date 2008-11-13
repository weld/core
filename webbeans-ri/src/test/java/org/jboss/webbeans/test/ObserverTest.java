package org.jboss.webbeans.test;
import static org.jboss.webbeans.test.util.Util.createSimpleBean;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Observes;
import javax.webbeans.Standard;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedMethod;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.mock.MockManagerImpl;
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
   private MockManagerImpl manager;
   private SimpleBean<Tuna> tuna;
   private AnnotatedMethod<Object> om;

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
      manager = new MockManagerImpl();
      manager.setEnabledDeploymentTypes(enabledDeploymentTypes);

      // Create an observer with known binding types
      // TODO This should be a real class being mapped
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      //AnnotatedClass<Tuna> annotatedItem = new SimpleAnnotatedClass<Tuna>(Tuna.class, annotations);
      
      tuna = createSimpleBean(Tuna.class, manager);
      om = new SimpleAnnotatedMethod<Object>(AnObserver.class.getMethod("observe", new Class[] { SampleEvent.class }), new SimpleAnnotatedClass<AnObserver>(AnObserver.class));
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
      /*Observer<SampleEvent> observer = new MockObserverImpl<SampleEvent>(tuna, om, SampleEvent.class);
      ((MockObserverImpl<SampleEvent>) observer).setInstance(observerInstance);
      SampleEvent event = new SampleEvent();
      observer.notify(event);
      assert observerInstance.notified;*/
   }

}
