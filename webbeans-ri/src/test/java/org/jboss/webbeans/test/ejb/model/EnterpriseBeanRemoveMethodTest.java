package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;
import javax.webbeans.RequestScoped;
import javax.webbeans.UnremovedException;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.ejb.model.invalid.Armant;
import org.jboss.webbeans.test.ejb.model.invalid.GoldenRetriever;
import org.jboss.webbeans.test.ejb.model.invalid.JackRussellTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.Poodle;
import org.jboss.webbeans.test.ejb.model.invalid.Pumi;
import org.jboss.webbeans.test.ejb.model.invalid.Rottweiler;
import org.jboss.webbeans.test.ejb.model.invalid.RussellTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.Saluki;
import org.jboss.webbeans.test.ejb.model.invalid.Spitz;
import org.jboss.webbeans.test.ejb.model.valid.Koirus;
import org.jboss.webbeans.test.ejb.model.valid.Toller;
import org.jboss.webbeans.test.ejb.model.valid.WelshCorgie;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

@SpecVersion("PDR")
@SuppressWarnings("unused")
public class EnterpriseBeanRemoveMethodTest extends AbstractTest
{

   public static boolean tickle = false;

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatelessEnterpriseBeansWithRemoveMethodsFails()
   {
      EnterpriseBean<Armant> bean = BeanFactory.createEnterpriseBean(Armant.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "lifecycle", "stub"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanRemoveMethodCalledWhenDestroyedByManager()
   {
      EnterpriseBeanRemoveMethodTest.tickle = false;
      EnterpriseBean<Toller> bena = BeanFactory.createEnterpriseBean(Toller.class);
      RequestContext context = (RequestContext) manager.getContext(RequestScoped.class);
      Toller instance = context.get(bena, true);
      context.destroy();
      assert EnterpriseBeanRemoveMethodTest.tickle;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeDependentScoped()
   {
      EnterpriseBean<Pumi> bean = BeanFactory.createEnterpriseBean(Pumi.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeDependentScoped2()
   {
      EnterpriseBean<WelshCorgie> bean = BeanFactory.createEnterpriseBean(WelshCorgie.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "stub"}, expectedExceptions = UnremovedException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeRemovedByApplicationBeforeManager()
   {
      EnterpriseBean<Toller> bean = BeanFactory.createEnterpriseBean(Toller.class);
      Toller instance = manager.getInstance(bean);
      RequestContext context = (RequestContext) manager
            .getContext(RequestScoped.class);
      context.destroy();
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "lifecycle", "stub"})
   @SpecAssertion(section = "3.3.5")
   public void testApplicationRemoveMethodCallRemovesInstanceFromContext()
   {
      EnterpriseBean<Toller> bean = BeanFactory.createEnterpriseBean(Toller.class);
      RequestContext context = (RequestContext) manager.getContext(RequestScoped.class);
      Toller instance = context.get(bean, true);
      instance.bye();
      instance = context.get(bean, false);
      assert instance == null;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testEnterpriseBeanWithoutDestructorUsesNoArgsRemoveAnnotatedMethodAsRemoveMethod()
   {
      EnterpriseBean<Toller> bean = BeanFactory.createEnterpriseBean(Toller.class);
      assert "bye".equals(bean.getRemoveMethod().getName());
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testEnterpriseBeanWithoutDestructorAndNoArgsRemoveAnnotatedMethodHasNoRemoveMethod()
   {
      EnterpriseBean<Koirus> bean = BeanFactory.createEnterpriseBean(Koirus.class);
      assert bean.getRemoveMethod() == null;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testMultipleDestructorAnnotatedMethodsFails()
   {
      EnterpriseBean<Rottweiler> bean = BeanFactory.createEnterpriseBean(Rottweiler.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testDestructorAnnotatedMethodNotRemoveAnnotatedFails()
   {
      EnterpriseBean<RussellTerrier> bean = BeanFactory.createEnterpriseBean(RussellTerrier.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodIsInitializerFails()
   {
      EnterpriseBean<Saluki> bean = BeanFactory.createEnterpriseBean(Saluki.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodIsProducerFails()
   {
      EnterpriseBean<Spitz> bean = BeanFactory.createEnterpriseBean(Spitz.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodWithDisposesParameterFails()
   {
      EnterpriseBean<GoldenRetriever> bean = BeanFactory.createEnterpriseBean(GoldenRetriever.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodWithObservesParameterFails()
   {
      EnterpriseBean<JackRussellTerrier> bean = BeanFactory.createEnterpriseBean(JackRussellTerrier.class);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testMultipleRemoveAnnotationsButNoDestructorFails()
   {
      EnterpriseBean<Poodle> bean = BeanFactory.createEnterpriseBean(Poodle.class);
   }
   
   
   @Test(groups={"enterpriseBeans", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanWithoutMatchingRemoveMethodFails()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanWithMultipleRemoveMethodsFails()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "stub"})
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanIgnoresBindingAnnotationOnParameters()
   {
      assert false;
   }

}
