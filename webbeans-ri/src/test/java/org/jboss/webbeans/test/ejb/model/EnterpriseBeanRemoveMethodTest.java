package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.bean.EnterpriseBean;
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
import org.jboss.webbeans.test.ejb.model.invalid.Toller;
import org.jboss.webbeans.test.ejb.model.invalid.WelshCorgie;
import org.jboss.webbeans.test.ejb.model.invalid.Whippet;
import org.jboss.webbeans.test.ejb.model.valid.Laika;
import org.jboss.webbeans.test.ejb.model.valid.Pitbull;
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

@SpecVersion("PDR")
@SuppressWarnings("unused")
public class EnterpriseBeanRemoveMethodTest extends AbstractTest
{

   @Test
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInDependentScopeOK()
   {
      EnterpriseBean<Pitbull> pitbull = Util.createEnterpriseBean(Pitbull.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInApplicationScopeFails()
   {
      EnterpriseBean<Poodle> poodle = Util.createEnterpriseBean(Poodle.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInSessionScopeFails()
   {
      EnterpriseBean<Rottweiler> rottweiler = Util.createEnterpriseBean(Rottweiler.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInConversationScopeFails()
   {
      EnterpriseBean<Spitz> spitz = Util.createEnterpriseBean(Spitz.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInRequestScopeFails()
   {
      EnterpriseBean<Saluki> saluki = Util.createEnterpriseBean(Saluki.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithoutDestructorUsesRemoveMethodWithoutParamsAsWebBeansRemoveMethod()
   {
      EnterpriseBean<Armant> whippet = Util.createEnterpriseBean(Armant.class, manager);
      assert whippet.getRemoveMethod().getAnnotatedItem().getName().equals("destroy");
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithoutDestructorAndRemoveMethodWithoutParamsHasNoWebBeansRemoveMethod()
   {
      EnterpriseBean<Laika> laika = Util.createEnterpriseBean(Laika.class, manager);
      assert laika.getRemoveMethod() == null;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithMultipleDestructorAnnotationsFail()
   {
      EnterpriseBean<Whippet> whippet = Util.createEnterpriseBean(Whippet.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithDestructorAnnotationOnMethodNotAnEJBRemoveMethodFails()
   {
      EnterpriseBean<GoldenRetriever> goldenRetriever = Util.createEnterpriseBean(GoldenRetriever.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithDestructorWithDisposesParameterFails()
   {
      EnterpriseBean<Pumi> pumi = Util.createEnterpriseBean(Pumi.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithInitializerAnnotationOnRemoveMethodFails()
   {
      EnterpriseBean<JackRussellTerrier> jackRussellTerrier = Util.createEnterpriseBean(JackRussellTerrier.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithProducerAnnotationOnRemoveMethodFails()
   {
      EnterpriseBean<RussellTerrier> russellTerrier = Util.createEnterpriseBean(RussellTerrier.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithRemoveMethodTakingObserverAnnotatedParameterFails()
   {
      EnterpriseBean<Toller> toller = Util.createEnterpriseBean(Toller.class, manager);
   }

   @Test(expectedExceptions=DefinitionException.class)
   public void testStatelessEnterpriseBeansWithDestructorAnnotationFails() 
   {
      EnterpriseBean<WelshCorgie> welshCorgie = Util.createEnterpriseBean(WelshCorgie.class, manager);
   }
   
   
   
}
