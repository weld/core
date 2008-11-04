package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.FoxRun;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class DependentContextTest extends AbstractTest
{
   
   @Test(groups={"contexts", "injection"}) @SpecAssertion(section="8.3")
   public void testInstanceNotSharedBetweenInjectionPoints()
   {
      Bean<FoxRun> foxRunBean = createSimpleWebBean(FoxRun.class, manager);
      Bean<Fox> foxBean = createSimpleWebBean(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert !foxRun.fox.equals(foxRun.anotherFox);
   }
   
   @Test(groups={"contexts", "el"}) @SpecAssertion(section="8.3")
   public void testInstanceUsedForElEvalutionNotShared()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.3")
   public void testInstanceUsedForProducerMethodNotShared()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "observerMethod"}) @SpecAssertion(section="8.3")
   public void testInstanceUsedForObserverMethodNotShared()
   {
      assert false;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.3")
   public void testContextGetWithCreateTrueReturnsNewInstance()
   {
      Bean<Fox> foxBean = createSimpleWebBean(Fox.class, manager);
      manager.addBean(foxBean);
      DependentContext context = new DependentContext();
      context.setActive(true);
      assert context.get(foxBean, true) != null;
      assert context.get(foxBean, true) instanceof Fox;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.3")
   public void testContextGetWithCreateFalseReturnsNull()
   {
      Bean<Fox> foxBean = createSimpleWebBean(Fox.class, manager);
      manager.addBean(foxBean);
      DependentContext context = new DependentContext();
      context.setActive(true);
      assert context.get(foxBean, false) == null;
   }
   
   @Test(groups="contexts", expectedExceptions=ContextNotActiveException.class) @SpecAssertion(section="8.3")
   public void testContextIsInactive()
   {
      manager.getContext(Dependent.class).isActive();
   }
   
   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveWhenInvokingProducerMethod()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "observerMethod"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveWhenInvokingObserverMethod()
   {
      assert false;
   }
   
   
   @Test(groups={"contexts", "el"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveWhenEvaluatingElExpression()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "beanLifecycle"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveDuringBeanCreation()
   {
      // Slightly roundabout, but I can't see a better way to test atm
      Bean<FoxRun> foxRunBean = createSimpleWebBean(FoxRun.class, manager);
      Bean<Fox> foxBean = createSimpleWebBean(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert foxRun.fox != null;
   }
   
   @Test(groups={"contexts", "beanDestruction"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveDuringBeanDestruction()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "injection"}) @SpecAssertion(section="8.3")
   public void testContextIsActiveDuringInjection()
   {
      Bean<FoxRun> foxRunBean = createSimpleWebBean(FoxRun.class, manager);
      Bean<Fox> foxBean = createSimpleWebBean(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert foxRun.fox != null;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testEjbBeanMayMayCreateInstanceFromInitializer()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testEjbBeanMayMayCreateInstanceFromPostConstruct()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testEjbBeanMayMayCreateInstanceFromPreDestroy()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "servlet"}) @SpecAssertion(section="8.3")
   public void testServletBeanMayMayCreateInstanceFromInitializer()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "beanDestruction"}) @SpecAssertion(section="8.3")
   public void testDestroyingParentDestroysDependents()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "ejb3"}) @SpecAssertion(section="8.3")
   public void testDestroyingEjbDestroysDependents()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "servlet"}) @SpecAssertion(section="8.3")
   public void testDestroyingServletDestroysDependents()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "el"}) @SpecAssertion(section="8.3")
   public void testDependentsDestroyedWhenElEvaluationCompletes()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.3")
   public void testDependentsDestroyedWhenProducerMethodEvaluationCompletes()
   {
      assert false;
   }
   
   @Test(groups={"contexts", "observerMethod"}) @SpecAssertion(section="8.3")
   public void testDependentsDestroyedWhenObserverMethodEvaluationCompletes()
   {
      assert false;
   }
   
}
