package org.jboss.webbeans.test;

import javax.webbeans.Observer;

import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.event.EventObserver;
import org.jboss.webbeans.test.beans.DangerCall;
import org.jboss.webbeans.test.bindings.AnimalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RoleBinding;
import org.jboss.webbeans.test.bindings.TameAnnotationLiteral;
import org.testng.annotations.Test;

/**
 * Unit tests for the wrapper class {@link EventObserverTest} which implements some of
 * the observer resolution behavior specified in 7.7.1 and 7.7.2 of the Web Beans
 * Specification.
 * 
 * @author David Allen
 *
 */
@SpecVersion("PDR")
public class EventObserverTest
{
   public class AnObserver<T> implements Observer<T>
   {
      public void notify(T event)
      {
      }
   }

   
   /**
    * Tests different annotation literals as event bindings to make sure the wrapper
    * properly detects when an observer matches the given event bindings.
    */
   @Test(groups = "event")
   @SpecAssertion(section = "7.7.1")
   public void testIsObserverInterested()
   {
      Observer<DangerCall> observer = new AnObserver<DangerCall>();
      EventObserver<DangerCall> wrappedObserver = new EventObserver<DangerCall>(new MetaDataCache(), observer, DangerCall.class, new TameAnnotationLiteral());
      assert wrappedObserver.getEventBindings().size() == 1;
      assert wrappedObserver.isObserverInterested(new TameAnnotationLiteral());
      assert !wrappedObserver.isObserverInterested(new AnimalStereotypeAnnotationLiteral());
      assert !wrappedObserver.isObserverInterested();
      assert wrappedObserver.isObserverInterested(new TameAnnotationLiteral(), new RoleBinding("Admin"));
      
      // Perform some tests with binding values (7.7.1)
      wrappedObserver = new EventObserver<DangerCall>(new MetaDataCache(), observer, DangerCall.class, new RoleBinding("Admin"));
      assert wrappedObserver.getEventBindings().size() == 1;
      assert wrappedObserver.isObserverInterested(new RoleBinding("Admin"));
      assert !wrappedObserver.isObserverInterested(new RoleBinding("User"));
   }
}
