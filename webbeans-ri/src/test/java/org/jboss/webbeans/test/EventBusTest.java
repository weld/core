package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Observer;
import javax.webbeans.ObserverException;
import javax.webbeans.TypeLiteral;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.test.beans.AuroraFinch;
import org.jboss.webbeans.test.beans.BananaSpider;
import org.jboss.webbeans.test.beans.BirdCage;
import org.jboss.webbeans.test.beans.BlueFacedParrotFinch;
import org.jboss.webbeans.test.beans.EuropeanGoldfinch;
import org.jboss.webbeans.test.beans.FinchKeeper;
import org.jboss.webbeans.test.beans.OrangeCheekedWaxbill;
import org.jboss.webbeans.test.beans.RecluseSpider;
import org.jboss.webbeans.test.beans.StarFinch;
import org.jboss.webbeans.test.beans.TeaCupPomeranian;
import org.jboss.webbeans.test.beans.broken.BlackRumpedWaxbill;
import org.jboss.webbeans.test.beans.broken.CommonWaxbill;
import org.jboss.webbeans.test.beans.broken.GoldbreastWaxbill;
import org.jboss.webbeans.test.beans.broken.JavaSparrow;
import org.jboss.webbeans.test.beans.broken.OwlFinch;
import org.jboss.webbeans.test.beans.broken.SweeWaxbill;
import org.jboss.webbeans.test.bindings.AnimalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RoleBinding;
import org.jboss.webbeans.test.bindings.TameAnnotationLiteral;
import org.jboss.webbeans.test.ejb.invalid.AustralianTerrier;
import org.jboss.webbeans.test.ejb.invalid.BorderTerrier;
import org.jboss.webbeans.test.ejb.invalid.BostonTerrier;
import org.jboss.webbeans.test.ejb.invalid.CairnsTerrier;
import org.jboss.webbeans.test.ejb.invalid.FoxTerrier;
import org.jboss.webbeans.test.ejb.invalid.TibetanTerrier;
import org.jboss.webbeans.test.ejb.invalid.YorkshireTerrier;
import org.jboss.webbeans.test.ejb.valid.BullTerrier;
import org.jboss.webbeans.test.ejb.valid.Pomeranian;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.testng.annotations.Test;

/**
 * Event bus tests
 * 
 * @author Nicklas Karlsson
 * @author David Allen
 * 
 */
@SpecVersion("20081206")
public class EventBusTest extends AbstractTest
{
   public static class AnEventType
   {
   }

   public static class ATemplatedEventType<T>
   {
   }

   public static class AnObserver implements Observer<AnEventType>
   {
      public boolean wasNotified = false;

      public void notify(AnEventType event)
      {
         wasNotified = true;
      }
   }

   public static class AnObserverWithException implements Observer<AnEventType>
   {
      public boolean wasNotified = false;
      public RuntimeException theException = new RuntimeException("RE1");

      public void notify(AnEventType event)
      {
         wasNotified = true;
         throw theException;
      }
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.1")
   public void testEventTypeIncludesAllSuperclassesAndInterfacesOfEventObject()
   {
      assert false;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.2")
   public void testManagerFireEvent()
   {
      // First a simple event with no bindings is fired
      AnEventType anEvent = new AnEventType();
      manager.fireEvent(anEvent);

      // Next an event with some event bindings is fired
      manager.fireEvent(anEvent, new RoleBinding("Admin"));
   }

   /**
    * If the type of the event object passed to fireEvent() contains type
    * variables or wildcards, an IllegalArgumentException is thrown
    */
   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = { "8.1", "8.2" })
   public void testManagerFireEventWithEventTypeParametersFails()
   {
      ATemplatedEventType<String> anEvent = new ATemplatedEventType<String>();
      manager.fireEvent(anEvent);
   }

   /**
    * If the type of the event object passed to fireEvent() contains type
    * variables or wildcards, an IllegalArgumentException is thrown
    */
   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = { "8.1", "8.2" })
   public void testManagerFireEventWithEventTypeWildcardsFails()
   {
      // Although the above test is really the same as with a wildcard, we will
      // test
      // it anyhow since the specification calls it out separately.
      ATemplatedEventType<?> anEventOnAnyType = new ATemplatedEventType<String>();
      manager.fireEvent(anEventOnAnyType);
   }

   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = { "8.1", "8.2" })
   public void testManagerFireEventWithNonBindingAnnotationsFails()
   {
      // The specs are not exactly clear on what is supposed to happen here,
      // but borrowing from Section 8.3, we'll expect the same behavior here
      // for a consistent API.
      // TODO Verify that fireEvent should fail on non-binding annotations
      AnEventType anEvent = new AnEventType();
      manager.fireEvent(anEvent, new AnimalStereotypeAnnotationLiteral());
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.3")
   public void testManagerAddObserver()
   {
      Observer<AnEventType> observer = new AnObserver();

      // First test with the Class<T> of the event type
      manager.addObserver(observer, AnEventType.class);
      Set<Observer<AnEventType>> resolvedObservers = manager.resolveObservers(new AnEventType());
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 1;
      assert resolvedObservers.iterator().next() == observer;

      // Now test with the TypeLiteral<T> of the event type
      observer = new AnObserver();
      manager.addObserver(observer, new TypeLiteral<AnEventType>()
      {
      });
      resolvedObservers = manager.resolveObservers(new AnEventType());
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 2;
      boolean foundObserver = false;
      for (Observer<AnEventType> obs : resolvedObservers)
      {
         if (obs == observer)
         {
            foundObserver = true;
            break;
         }
      }
      assert foundObserver;

      // Try adding an observer with some binding types
      observer = new AnObserver();
      Annotation[] bindingTypes = new Annotation[] { new RoleBinding("Admin"), new RoleBinding("Manager") };
      manager.addObserver(observer, AnEventType.class, bindingTypes);
      resolvedObservers = manager.resolveObservers(new AnEventType(), bindingTypes);
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 3;
      foundObserver = false;
      for (Observer<AnEventType> obs : resolvedObservers)
      {
         if (obs == observer)
         {
            foundObserver = true;
            break;
         }
      }
      assert foundObserver;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.3")
   public void testManagerRemoveObserver()
   {
      Observer<AnEventType> observer = new AnObserver();

      // First test with the Class<T> of the event type
      manager.addObserver(observer, AnEventType.class);
      manager.removeObserver(observer, AnEventType.class);
      Set<Observer<AnEventType>> resolvedObservers = manager.resolveObservers(new AnEventType());
      assert resolvedObservers.isEmpty();

      // Now test with the TypeLiteral<T> of the event type
      observer = new AnObserver();
      manager.addObserver(observer, new TypeLiteral<AnEventType>()
      {
      });
      manager.removeObserver(observer, new TypeLiteral<AnEventType>()
      {
      });
      resolvedObservers = manager.resolveObservers(new AnEventType());
      assert resolvedObservers.isEmpty();

      // Also test with binding types
      Annotation[] bindings = new Annotation[] { new RoleBinding("Admin") };
      manager.addObserver(observer, AnEventType.class, bindings);
      manager.removeObserver(observer, AnEventType.class);
      resolvedObservers = manager.resolveObservers(new AnEventType(), bindings);
      assert !resolvedObservers.isEmpty();
      manager.removeObserver(observer, AnEventType.class, new RoleBinding("Admin"));
      resolvedObservers = manager.resolveObservers(new AnEventType(), bindings);
      assert resolvedObservers.isEmpty();
   }

   @Test(groups = { "events" }, expectedExceptions = { DuplicateBindingTypeException.class })
   @SpecAssertion(section = "8.3")
   public void testMultipleInstancesOfSameBindingTypeWhenAddingObserverFails()
   {
      Observer<AnEventType> observer = new AnObserver();
      manager.addObserver(observer, AnEventType.class, new RoleBinding("Admin"), new TameAnnotationLiteral(), new TameAnnotationLiteral());
   }

   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = "8.3")
   public void testNonBindingTypePassedToAddObserverFails()
   {
      Observer<AnEventType> observer = new AnObserver();
      manager.addObserver(observer, AnEventType.class, new AnimalStereotypeAnnotationLiteral());
   }

   @Test(groups = { "events" }, expectedExceptions = { DuplicateBindingTypeException.class })
   @SpecAssertion(section = "8.3")
   public void testMultipleInstancesOfSameBindingTypeWhenRemovingObserverFails()
   {
      Observer<AnEventType> observer = new AnObserver();
      manager.addObserver(observer, AnEventType.class);
      manager.removeObserver(observer, AnEventType.class, new RoleBinding("Admin"), new TameAnnotationLiteral(), new TameAnnotationLiteral());
   }

   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = "8.3")
   public void testNonBindingTypePassedToRemoveObserverFails()
   {
      Observer<AnEventType> observer = new AnObserver();
      manager.addObserver(observer, AnEventType.class);
      manager.removeObserver(observer, AnEventType.class, new AnimalStereotypeAnnotationLiteral());
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = { "8.1", "8.4" })
   public void testConsumerNotifiedWhenEventTypeAndAllBindingsMatch()
   {
      AnObserver observer1 = new AnObserver();
      AnObserver observer2 = new AnObserver();
      manager.addObserver(observer1, AnEventType.class);
      manager.addObserver(observer2, AnEventType.class);

      // Fire an event that will be delivered to the two above observers
      AnEventType anEvent = new AnEventType();
      manager.fireEvent(anEvent);

      assert observer1.wasNotified;
      assert observer2.wasNotified;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.4")
   public void testObserverThrowsExceptionAbortsNotifications()
   {
      AnObserver observer = new AnObserver();
      AnObserverWithException anotherObserver = new AnObserverWithException();
      manager.addObserver(anotherObserver, AnEventType.class);
      manager.addObserver(observer, AnEventType.class);

      // Fire an event that will be delivered to the two above observers
      AnEventType anEvent = new AnEventType();
      boolean fireFailed = false;
      try
      {
         manager.fireEvent(anEvent);
      }
      catch (Exception e)
      {
         if (anotherObserver.theException.equals(e))
            fireFailed = true;
      }
      assert fireFailed;

      assert anotherObserver.wasNotified;
      // TODO This cannot properly test for event processing abort
      // assert !observer.wasNotified;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.4")
   public void testObserverCalledBeforeTransactionCompleteMaySetRollbackOnly()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.4")
   public void testObserverManipulatingJTATransactionsDirectlyFails()
   {
      assert false;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5")
   public void testObserverMethodOnEnterpriseBeanIsBusinessMethodOrStatic()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(Pomeranian.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans.size() == 1 + MockManagerImpl.BUILT_IN_BEANS;
      Set<Observer<String>> observers = manager.resolveObservers("An event");
      assert observers.size() == 2;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5")
   public void testObserverMethodOnEnterpriseBeanNotBusinessMethodOrStaticFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(TibetanTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans.size() == 1 + MockManagerImpl.BUILT_IN_BEANS;
      Set<Observer<String>> observers = manager.resolveObservers("An event");
      assert observers.size() == 1;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5")
   public void testMultipleObserverMethodsOK()
   {
      // Somewhat of a cheat, but this other test already does have 2 observer
      // methods
      // for the same event type and event bindings.
      testObserverMethodOnEnterpriseBeanIsBusinessMethodOrStatic();
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = { "8.5.1", "8.5.2" })
   public void testObserverMethodMustHaveOnlyOneEventParameter()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(YorkshireTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.5.1")
   public void testObserverMethodCannotObserveParameterizedEvents()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(BostonTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.1")
   public void testObserverMethodWithoutBindingTypesObservesEventsWithoutBindingTypes()
   {
      // This observer has no binding types specified
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(Pomeranian.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans.size() == 1 + MockManagerImpl.BUILT_IN_BEANS;

      // Resolve registered observers with an event containing no binding types
      Set<Observer<String>> resolvedObservers = manager.resolveObservers("A new event");
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 2;
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodAnnotatedProducesFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(BorderTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodAnnotatedInitializerFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(AustralianTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodAnnotatedDestructorFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(CairnsTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodWithDisposesParamFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FoxTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodMayHaveMultipleBindingTypes()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(BullTerrier.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
      // If we can resolve the observer with the two binding types,
      // then it worked
      Set<Observer<String>> resolvedObservers = manager.resolveObservers("An event object", new RoleBinding("Admin"), new TameAnnotationLiteral());
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 1;

   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.3")
   public void testXMLDefinedObserverMethodIgnoresBindingAnnotations()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.3")
   public void testXMLDefinedObserverNotFindingImplementationMethodFails()
   {
      assert false;
   }
   
   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.4")
   public void testObserverMethodReceivesInjectionsOnNonObservesParameters()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(BananaSpider.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans != null;
   }

   /**
    * Tests that a conditional observer is not notified of events until after it
    * is created by some other separate action.
    */
   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.5")
   public void testConditionalObserver()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(RecluseSpider.class));
      webBeansBootstrap.boot();

      manager.fireEvent("New string event");
      // Should not be notified since bean is not instantiated yet
      assert !RecluseSpider.notified;
      
      // Now instantiate the bean and fire another event
      try
      {
         DependentContext.INSTANCE.setActive(true);
         RecluseSpider bean = manager.getInstanceByType(RecluseSpider.class);
         assert bean != null;
         
         manager.fireEvent("Another event");
         assert RecluseSpider.notified;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.1")
   public void testTransactionalObserverCanOnlyObserveSinglePhase()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.6")
   public void testTransactionalObserverNotifiedImmediatelyWhenNoTransactionInProgress()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.6")
   public void testAfterTransactionCompletionObserver()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.6")
   public void testAfterTransactionSuccessObserver()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.6")
   public void testAfterTransactionFailureObserver()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.6")
   public void testBeforeTransactionCompletionObserver()
   {
      assert false;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.7")
   public void testObserverMethodRegistration()
   {
      // For now, this test is checking the registration of methods
      testObserverMethodOnEnterpriseBeanIsBusinessMethodOrStatic();
   }

   /**
    * 
    */
   @Test(groups = { "broken", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testEnterpriseBeanObserverMethodCalledWithCallerContext()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(Pomeranian.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans.size() == 1;
      String event = "A new event";
      Set<Observer<String>> observers = manager.resolveObservers(event);
      assert observers.size() == 1;
      
      manager.fireEvent(event);
      assert Thread.currentThread().equals(Pomeranian.notificationThread);
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testTransactionalObserverThrownExceptionIsCaughtAndLogged()
   {
      assert false;
   }

   @Test(groups = { "events" }, expectedExceptions={ TeaCupPomeranian.OversizedException.class })
   @SpecAssertion(section = "8.5.7")
   public void testNonTransactionalObserverThrownNonCheckedExceptionIsRethrown()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(TeaCupPomeranian.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans.size() == 1 + MockManagerImpl.BUILT_IN_BEANS;
      manager.fireEvent("Another event");
   }

   @Test(groups = { "events" }, expectedExceptions={ ObserverException.class })
   @SpecAssertion(section = "8.5.7")
   public void testNonTransactionalObserverThrownCheckedExceptionIsWrappedAndRethrown()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(TeaCupPomeranian.class));
      webBeansBootstrap.boot();
      List<Bean<?>> beans = manager.getBeans();
      assert beans.size() == 1 + MockManagerImpl.BUILT_IN_BEANS;
      manager.fireEvent(new Integer(1));
   }

   @Test(groups = { "events" }, expectedExceptions = { DuplicateBindingTypeException.class })
   @SpecAssertion(section = "8.6")
   public void testDuplicateBindingsToFireFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(SweeWaxbill.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         SweeWaxbill bean = manager.getInstanceByType(SweeWaxbill.class);
         bean.methodThatFiresEvent();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions={ DuplicateBindingTypeException.class })
   @SpecAssertion(section = "8.6")
   public void testDuplicateBindingsToObservesFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(SweeWaxbill.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         SweeWaxbill bean = manager.getInstanceByType(SweeWaxbill.class);
         bean.methodThatRegistersObserver();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = "8.6")
   public void testNonBindingTypePassedToFireFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(OwlFinch.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         OwlFinch bean = manager.getInstanceByType(OwlFinch.class);
         bean.methodThatFiresEvent();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions = { IllegalArgumentException.class })
   @SpecAssertion(section = "8.6")
   public void testNonBindingTypePassedToObservesFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(OwlFinch.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         OwlFinch bean = manager.getInstanceByType(OwlFinch.class);
         bean.methodThatRegistersObserver();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnField()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(BlueFacedParrotFinch.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         BlueFacedParrotFinch bean = manager.getInstanceByType(BlueFacedParrotFinch.class);
         bean.methodThatRegistersObserver();

         Set<Observer<String>> observers = manager.resolveObservers("String type event");
         assert observers.size() == 1;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfProducerMethod()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(StarFinch.class, FinchKeeper.class, BirdCage.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         StarFinch starFinch = manager.getInstanceByType(StarFinch.class);
         FinchKeeper birdKeeper = manager.getInstanceByType(FinchKeeper.class);
         BirdCage birdCage = manager.getInstanceByType(BirdCage.class);
         assert starFinch != null;
         assert birdCage != null;
         assert birdCage.getSomeMess() != null;
         assert birdKeeper.isNewMessDetected();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events", "broken" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfInitializerMethod()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(OrangeCheekedWaxbill.class, FinchKeeper.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         OrangeCheekedWaxbill bird = manager.getInstanceByType(OrangeCheekedWaxbill.class);
         FinchKeeper birdKeeper = manager.getInstanceByType(FinchKeeper.class);
         assert bird != null;
         assert bird.getSomeMess() != null;
         assert birdKeeper.isNewMessDetected();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   //TODO Implement once disposal methods are included
   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfDisposalMethod()
   {
      assert false;
   }

   //TODO Make sure this test works once EJBs are fully supported
   @Test(groups = { "broken", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfRemoveMethod()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(EuropeanGoldfinch.class, FinchKeeper.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         EuropeanGoldfinch bird = manager.getInstanceByType(EuropeanGoldfinch.class);
         FinchKeeper birdKeeper = manager.getInstanceByType(FinchKeeper.class);
         assert bird != null;
         
         assert birdKeeper.isNewMessDetected();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfConstructor()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(AuroraFinch.class, FinchKeeper.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         AuroraFinch bird = manager.getInstanceByType(AuroraFinch.class);
         FinchKeeper birdKeeper = manager.getInstanceByType(FinchKeeper.class);
         assert bird != null;
         assert birdKeeper.isNewMessDetected();
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnNonEventTypeInjectionPointFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(CommonWaxbill.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         CommonWaxbill bean = manager.getInstanceByType(CommonWaxbill.class);
         assert bean != null;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnInjectionPointWithoutTypeParameterFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(BlackRumpedWaxbill.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         BlackRumpedWaxbill bean = manager.getInstanceByType(BlackRumpedWaxbill.class);
         webBeansBootstrap.boot();
         assert bean != null;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnInjectionPointWithWildcardedTypeParameterFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(GoldbreastWaxbill.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         GoldbreastWaxbill bean = manager.getInstanceByType(GoldbreastWaxbill.class);
         assert bean != null;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "events" }, expectedExceptions = { DefinitionException.class })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnInjectionPointWithTypeVariabledTypeParameterFails()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(JavaSparrow.class));
      webBeansBootstrap.boot();
      try
      {
         DependentContext.INSTANCE.setActive(true);
         JavaSparrow bean = manager.getInstanceByType(JavaSparrow.class);
         assert bean != null;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testImplicitObserverBeanMatchesAPITypeOfInectionPoint()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testImplicitObserverBeanMatchesBindingAnnotationsOfInjectionPoint()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testImplicitObserverBeanHasStandardDeploymentType()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testImplicitObserverBeanHasDependentScope()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testFireMethodCallsManagerFireWithEventObject()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testFireMethodCallsManagerFireWithBindingAnnotationsExceptObservable()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testFireMethodCallsManagerFireWithAllBindingAnnotationInstances()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testObserveMethodCallsManagerAddObserverWithObserverObject()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testObserveMethodCallsManagerAddObserverWithAllBindingAnnotationsExceptObservable()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.6")
//   public void testObserveMethodCallsManagerAddObserverWithAllBindingAnnotationInstance()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testEventObjectContainsTypeVariablesWhenResolvingFails()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testEventObjectContainsWildcardsWhenResolvingFails()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testDuplicateBindingTypesWhenResolvingFails()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testNonBindingTypeAnnotationWhenResolvingFails()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testResolvingChecksEventType()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testResolvingChecksTypeParameters()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testResolvingChecksBindingTypes()
//   {
//      assert false;
//   }
//
//   @Test(groups = { "stub", "events" })
//   @SpecAssertion(section = "8.7")
//   public void testResolvingChecksBindingTypeMembers()
//   {
//      assert false;
//   }

}
