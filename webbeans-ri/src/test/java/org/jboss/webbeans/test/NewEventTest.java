package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Event;
import javax.webbeans.Observer;
import javax.webbeans.TypeLiteral;

import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bindings.InitializedBinding;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.test.bindings.AnimalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RoleBinding;
import org.jboss.webbeans.test.bindings.TameAnnotationLiteral;
import org.jboss.webbeans.test.ejb.model.invalid.AustralianTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.BorderTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.BostonTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.CairnsTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.FoxTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.TibetanTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.YorkshireTerrier;
import org.jboss.webbeans.test.ejb.model.valid.Pomeranian;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

/**
 * Temporary name until synchronized with David Allen
 * 
 * @author Nicklas Karlsson
 * @author David Allen
 * 
 */
@SpecVersion("20081206")
public class NewEventTest extends AbstractTest
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

   @SuppressWarnings("unchecked")
   @Test(groups = "events")
   public void testEventBeanCreation()
   {
      SimpleBean<MyTest> myTestBean = BeanFactory.createSimpleBean(MyTest.class);
      for (AnnotatedField<Object> field : myTestBean.getEventFields())
      {
         EventBean eventBean = BeanFactory.createEventBean(field);
         Event<Param> event = eventBean.create();
         assert event != null;
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

   @Test(groups = { "events" })
   @SpecAssertion(section = { "8.1", "8.2" })
   public void testManagerFireEventWithParameterizedEventFails()
   {
      boolean fireEventFailed = false;
      try
      {
         ATemplatedEventType<String> anEvent = new ATemplatedEventType<String>();
         manager.fireEvent(anEvent);
      }
      catch (IllegalArgumentException e)
      {
         fireEventFailed = true;
      }
      assert fireEventFailed;

      // Although the above is really the same as with a wildcard, we will test
      // it anyhow since the specification calls it out separately.
      fireEventFailed = false;
      try
      {
         ATemplatedEventType<?> anEventOnAnyType = new ATemplatedEventType<String>();
         manager.fireEvent(anEventOnAnyType);
      }
      catch (IllegalArgumentException e)
      {
         fireEventFailed = true;
      }
      assert fireEventFailed;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = { "8.1", "8.2" })
   public void testManagerFireEventWithNonBindingAnnotationsFails()
   {
      // The specs are not exactly clear on what is supposed to happen here, but
      // borrowing
      // from Section 8.3, we'll expect the same behavior here for a consistent
      // API.
      // TODO Verify that fireEvent should fail on non-binding annotations
      boolean fireEventFailed = false;
      try
      {
         AnEventType anEvent = new AnEventType();
         manager.fireEvent(anEvent, new AnimalStereotypeAnnotationLiteral());
      }
      catch (IllegalArgumentException e)
      {
         fireEventFailed = true;
      }
      assert fireEventFailed;
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

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.3")
   public void testMultipleInstancesOfSameBindingTypeWhenAddingObserverFails()
   {
      boolean failedAddingObserver = false;
      try
      {
         Observer<AnEventType> observer = new AnObserver();
         manager.addObserver(observer, AnEventType.class, new RoleBinding("Admin"), new TameAnnotationLiteral(), new TameAnnotationLiteral());
      }
      catch (DuplicateBindingTypeException e)
      {
         failedAddingObserver = true;
      }
      assert failedAddingObserver;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.3")
   public void testNonBindingTypePassedToAddObserverFails()
   {
      boolean failedAddingObserver = false;
      try
      {
         Observer<AnEventType> observer = new AnObserver();
         manager.addObserver(observer, AnEventType.class, new AnimalStereotypeAnnotationLiteral());
      }
      catch (IllegalArgumentException e)
      {
         failedAddingObserver = true;
      }
      assert failedAddingObserver;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.3")
   public void testMultipleInstancesOfSameBindingTypeWhenRemovingObserverFails()
   {
      boolean failedRemovingObserver = false;
      try
      {
         Observer<AnEventType> observer = new AnObserver();
         manager.addObserver(observer, AnEventType.class);
         manager.removeObserver(observer, AnEventType.class, new RoleBinding("Admin"), new TameAnnotationLiteral(), new TameAnnotationLiteral());
      }
      catch (DuplicateBindingTypeException e)
      {
         failedRemovingObserver = true;
      }
      assert failedRemovingObserver;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.3")
   public void testNonBindingTypePassedToRemoveObserverFails()
   {
      boolean failedAddingObserver = false;
      try
      {
         Observer<AnEventType> observer = new AnObserver();
         manager.addObserver(observer, AnEventType.class);
         manager.removeObserver(observer, AnEventType.class, new AnimalStereotypeAnnotationLiteral());
      }
      catch (IllegalArgumentException e)
      {
         failedAddingObserver = true;
      }
      assert failedAddingObserver;
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
      Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(Pomeranian.class);
      assert beans.size() == 1;
      Set<Observer<MockManagerImpl>> observers = manager.resolveObservers(manager, new InitializedBinding());
      assert observers.size() == 2;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5")
   public void testObserverMethodOnEnterpriseBeanNotBusinessMethodOrStaticFails()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(TibetanTerrier.class);
      assert beans.size() == 1;
      Set<Observer<MockManagerImpl>> observers = manager.resolveObservers(manager, new InitializedBinding());
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

   @Test(groups = { "events" })
   @SpecAssertion(section = { "8.5.1", "8.5.2" })
   public void testObserverMethodMustHaveOnlyOneEventParameter()
   {
      boolean definitionException = false;
      try
      {
         Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(YorkshireTerrier.class);
         assert beans != null;
      }
      catch (DefinitionException e)
      {
         definitionException = true;
      }
      assert definitionException;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.1")
   public void testObserverMethodCannotObserveParameterizedEvents()
   {
      boolean definitionException = false;
      try
      {
         Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(BostonTerrier.class);
         assert beans != null;
      }
      catch (DefinitionException e)
      {
         definitionException = true;
      }
      assert definitionException;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodAnnotatedProducesFails()
   {
      boolean definitionException = false;
      try
      {
         Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(BorderTerrier.class);
         assert beans != null;
      }
      catch (DefinitionException e)
      {
         definitionException = true;
      }
      assert definitionException;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodAnnotatedInitializerFails()
   {
      boolean definitionException = false;
      try
      {
         Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(AustralianTerrier.class);
         assert beans != null;
      }
      catch (DefinitionException e)
      {
         definitionException = true;
      }
      assert definitionException;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodAnnotatedDestructorFails()
   {
      boolean definitionException = false;
      try
      {
         Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(CairnsTerrier.class);
         assert beans != null;
      }
      catch (DefinitionException e)
      {
         definitionException = true;
      }
      assert definitionException;
   }

   @Test(groups = { "events" })
   @SpecAssertion(section = "8.5.2")
   public void testObserverMethodWithDisposesParamFails()
   {
      boolean definitionException = false;
      try
      {
         Set<AbstractBean<?, ?>> beans = bootstrap.createBeans(FoxTerrier.class);
         assert beans != null;
      }
      catch (DefinitionException e)
      {
         definitionException = true;
      }
      assert definitionException;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.1")
   public void testObserverMethodWithoutBindingTypesObservesEventsWithoutBindingTypes()
   {
      assert false;
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

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.4")
   public void testObserverMethodReceivesInjectionsOnNonObservesParameters()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.5")
   public void testConditionalObserver()
   {
      assert false;
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

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testObserverMethodRegistration()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testEnterpriseBeanObserverMethodCalledWithCallerContext()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testTransactionalObserverThrownExceptionIsCaughtAndLogged()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testNonTransactionalObserverThrownNonCheckedExceptionIsRethrown()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.5.7")
   public void testNonTransactionalObserverThrownCheckedExceptionIsWrappedAndRethrown()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testDuplicateBindingsToFireFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testDuplicateBindingsToObservesFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testNonBindingTypePassedToFireFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testNonBindingTypePassedToObservesFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnField()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfProducerMethod()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfInitializerMethod()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfDisposalMethod()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfRemoveMethod()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnParameterOfConstructor()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableAnnotationOnNonEventTypeInjectionPointFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableannotationOnInjectionPointWithoutTypeParameterFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableannotationOnInjectionPointWithWildcardedTypeParameterFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObservableannotationOnInjectionPointWithTypeVariabledTypeParameterFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testImplicitObserverBeanMatchesAPITypeOfInectionPoint()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testImplicitObserverBeanMatchesBindingAnnotationsOfInjectionPoint()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testImplicitObserverBeanHasStandardDeploymentType()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testImplicitObserverBeanHasDependentScope()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testFireMethodCallsManagerFireWithEventObject()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testFireMethodCallsManagerFireWithBindingAnnotationsExceptObservable()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testFireMethodCallsManagerFireWithAllBindingAnnotationInstances()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObserveMethodCallsManagerAddObserverWithObserverObject()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObserveMethodCallsManagerAddObserverWithAllBindingAnnotationsExceptObservable()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.6")
   public void testObserveMethodCallsManagerAddObserverWithAllBindingAnnotationInstance()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testEventObjectContainsTypeVariablesWhenResolvingFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testEventObjectContainsWildcardsWhenResolvingFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testDuplicateBindingTypesWhenResolvingFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testNonBindingTypeAnnotationWhenResolvingFails()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testResolvingChecksEventType()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testResolvingChecksTypeParameters()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testResolvingChecksBindingTypes()
   {
      assert false;
   }

   @Test(groups = { "stub", "events" })
   @SpecAssertion(section = "8.7")
   public void testResolvingChecksBindingTypeMembers()
   {
      assert false;
   }

}
