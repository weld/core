package org.jboss.webbeans.test;

import java.lang.reflect.Method;

import javax.webbeans.Observer;
import javax.webbeans.Observes;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedMethodImpl;
import org.jboss.webbeans.test.annotations.Role;
import org.jboss.webbeans.test.bindings.RoleBinding;
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
public class ObserverTest extends AbstractTest
{
   
   // private SimpleBean<Tuna> tuna;
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

      public void observe(@Observes @Role("Admin") SampleEvent e)
      {
         // An observer method
         notified = true;
      }

   }

   public static @interface Foo
   {
   }

   @BeforeMethod
   public void beforeObserverTest() throws Exception
   {
      super.before();
      ob = BeanFactory.createSimpleBean(SampleObserver.class, manager);
      manager.addBean(ob);
      Method method = SampleObserver.class.getMethod("observe", SampleEvent.class);
      om = new AnnotatedMethodImpl<Object>(method, new AnnotatedClassImpl<SampleObserver>(SampleObserver.class));
      observer = BeanFactory.createObserver(om, ob, manager);
      manager.addObserver(observer, SampleEvent.class, new RoleBinding("Admin"));
      notified = false;
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.ObserverImpl#notify(javax.webbeans.Container, java.lang.Object)}
    * .
    */
   @Test(groups = "observerMethod")
   @SpecAssertion(section = { "7.5.7" })
   public final void testNotify() throws Exception
   {
      SampleEvent event = new SampleEvent();
      notified = false;
      observer.notify(event);
      assert notified == true;
   }

   @Test(groups = "observerMethod")
   @SpecAssertion(section = { "7.5.7" })
   public final void testNotifyViaManager() throws Exception
   {
      notified = false;
      manager.fireEvent(new SampleEvent());
      assert notified == false;
      manager.fireEvent(new Object(), new RoleBinding("Admin"));
      assert notified == false;
      manager.fireEvent(new SampleEvent(), new RoleBinding("Admin"));
      assert notified == true;
      notified = false;
      manager.fireEvent(new SampleEvent(), new RoleBinding("User"));
      assert notified == false;
      notified = false;
      manager.fireEvent(new SampleEvent(), new RoleBinding("Admin"), new RoleBinding("User"));
      assert notified == true;
   }

}
