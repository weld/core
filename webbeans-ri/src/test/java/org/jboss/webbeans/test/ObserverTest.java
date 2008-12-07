package org.jboss.webbeans.test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Observer;
import javax.webbeans.Observes;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedMethodImpl;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.util.BeanFactory;
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
   //private SimpleBean<Tuna> tuna;
   private SimpleBean<SampleObserver> ob;
   private AnnotatedMethod<Object> om;
   Observer<SampleEvent> observer;

   private static boolean notified = false;

   public static class SampleEvent
   {
      // Simple class used for testing
   }

   public static class SampleObserver
   {

      public void observe(@Observes @Asynchronous SampleEvent e)
      {
         // An observer method
         notified = true;
      }
      
   }
   
   public static @interface Foo {}

   @BeforeMethod
   public void before() throws Exception
   {
      manager = new MockManagerImpl();
      CurrentManager.setRootManager(manager);
      ob = BeanFactory.createSimpleBean(SampleObserver.class);
      manager.addBean(ob);
      Method method = SampleObserver.class.getMethod("observe", SampleEvent.class);
      om = new AnnotatedMethodImpl<Object>(method, new AnnotatedClassImpl<SampleObserver>(SampleObserver.class));
      observer = BeanFactory.createObserver( om, ob);
      Annotation annotation = method.getParameterAnnotations()[0][1];
      manager.addObserver(observer, SampleEvent.class, annotation);
      notified = false;
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.ObserverImpl#notify(javax.webbeans.Container, java.lang.Object)}
    * .
    */
   @Test(groups = "observerMethod") @SpecAssertion(section={"7.5.7"})
   public final void testNotify() throws Exception
   {
	  SampleEvent event = new SampleEvent();
	  notified = false;
      observer.notify(event);
      assert notified == true;
   }

   @Test(groups = "observerMethod") @SpecAssertion(section={"7.5.7"})
   public final void testNotifyViaManager() throws Exception
   {
	  notified = false;
      manager.fireEvent(new SampleEvent());
      assert notified == false;
      manager.fireEvent(new Object(), new AnnotationLiteral<Asynchronous>() {});
      assert notified == false;
      manager.fireEvent(new SampleEvent(), new AnnotationLiteral<Foo>() {});
      assert notified == false;
      manager.fireEvent(new SampleEvent(), new AnnotationLiteral<Asynchronous>() {});
      assert notified == true;
      notified = false;
      manager.fireEvent(new SampleEvent(), new AnnotationLiteral<Asynchronous>() {}, new AnnotationLiteral<Foo>() {});
      assert notified == true;
   }

}
