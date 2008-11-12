package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;
import javax.webbeans.RequestScoped;
import javax.webbeans.UnremovedException;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.XmlEnterpriseBean;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.ejb.model.invalid.Armant;
import org.jboss.webbeans.test.ejb.model.invalid.ChowChow;
import org.jboss.webbeans.test.ejb.model.invalid.GoldenRetriever;
import org.jboss.webbeans.test.ejb.model.invalid.JackRussellTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.Poodle;
import org.jboss.webbeans.test.ejb.model.invalid.Pumi;
import org.jboss.webbeans.test.ejb.model.invalid.Rottweiler;
import org.jboss.webbeans.test.ejb.model.invalid.RussellTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.Saluki;
import org.jboss.webbeans.test.ejb.model.invalid.Spitz;
import org.jboss.webbeans.test.ejb.model.valid.Toller;
import org.jboss.webbeans.test.ejb.model.valid.WelshCorgie;
import org.jboss.webbeans.test.ejb.model.valid.Koirus;
import org.jboss.webbeans.test.util.Util;
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
      EnterpriseBean<Armant> bean = Util.createEnterpriseBean(Armant.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "lifecycle"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanRemoveMethodCalledWhenDestroyedByManager()
   {
      EnterpriseBeanRemoveMethodTest.tickle = false;
      EnterpriseBean<Toller> bena = Util.createEnterpriseBean(Toller.class, manager);
      RequestContext context = (RequestContext) manager.getContext(RequestScoped.class);
      Toller instance = context.get(bena, true);
      context.destroy(manager);
      assert EnterpriseBeanRemoveMethodTest.tickle;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeDependentScoped()
   {
      EnterpriseBean<Pumi> bean = Util.createEnterpriseBean(Pumi.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeDependentScoped2()
   {
      EnterpriseBean<WelshCorgie> bean = Util.createEnterpriseBean(WelshCorgie.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = UnremovedException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeRemovedByApplicationBeforeManager()
   {
      EnterpriseBean<Toller> bean = Util.createEnterpriseBean(Toller.class, manager);
      Toller instance = manager.getInstance(bean);
      RequestContext context = (RequestContext) manager
            .getContext(RequestScoped.class);
      context.destroy(manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod", "lifecycle"})
   @SpecAssertion(section = "3.3.5")
   public void testApplicationRemoveMethodCallRemovesInstanceFromContext()
   {
      EnterpriseBean<Toller> bean = Util.createEnterpriseBean(Toller.class, manager);
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
      EnterpriseBean<Toller> bean = Util.createEnterpriseBean(Toller.class, manager);
      assert "bye".equals(bean.getRemoveMethod().getAnnotatedItem()
            .getAnnotatedMethod().getName());
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testEnterpriseBeanWithoutDestructorAndNoArgsRemoveAnnotatedMethodHasNoRemoveMethod()
   {
      EnterpriseBean<Koirus> bean = Util.createEnterpriseBean(Koirus.class, manager);
      assert bean.getRemoveMethod() == null;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testMultipleDestructorAnnotatedMethodsFails()
   {
      EnterpriseBean<Rottweiler> bean = Util.createEnterpriseBean(Rottweiler.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testDestructorAnnotatedMethodNotRemoveAnnotatedFails()
   {
      EnterpriseBean<RussellTerrier> bean = Util.createEnterpriseBean(RussellTerrier.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodIsInitializerFails()
   {
      EnterpriseBean<Saluki> bean = Util.createEnterpriseBean(Saluki.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodIsProducerFails()
   {
      EnterpriseBean<Spitz> bean = Util.createEnterpriseBean(Spitz.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodWithDisposesParameterFails()
   {
      EnterpriseBean<GoldenRetriever> bean = Util.createEnterpriseBean(GoldenRetriever.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testRemoveMethodWithObservesParameterFails()
   {
      EnterpriseBean<JackRussellTerrier> bean = Util.createEnterpriseBean(JackRussellTerrier.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = {"3.3.5.1", "3.3.5.2"})
   public void testMultipleRemoveAnnotationsButNoDestructorFails()
   {
      EnterpriseBean<Poodle> bean = Util.createEnterpriseBean(Poodle.class, manager);
   }
   
   
   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanWithoutMatchingRemoveMethodFails()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanWithMultipleRemoveMethodsFails()
   {
      XmlEnterpriseBean<ChowChow> bean = Util.createXmlEnterpriseBean(ChowChow.class, manager);
      assert false;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanIgnoresBindingAnnotationOnParameters()
   {
      assert false;
   }

}
