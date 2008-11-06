package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;
import javax.webbeans.UnremovedException;

import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
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
      EnterpriseBeanModel<Pitbull> pitbull = Util.createEnterpriseBeanModel(Pitbull.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInApplicationScopeFails()
   {
      EnterpriseBeanModel<Poodle> poodle = Util.createEnterpriseBeanModel(Poodle.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInSessionScopeFails()
   {
      EnterpriseBeanModel<Rottweiler> rottweiler = Util.createEnterpriseBeanModel(Rottweiler.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInConversationScopeFails()
   {
      EnterpriseBeanModel<Spitz> spitz = Util.createEnterpriseBeanModel(Spitz.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInRequestScopeFails()
   {
      EnterpriseBeanModel<Saluki> saluki = Util.createEnterpriseBeanModel(Saluki.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithoutDestructorUsesRemoveMethodWithoutParamsAsWebBeansRemoveMethod()
   {
      EnterpriseBeanModel<Armant> whippet = Util.createEnterpriseBeanModel(Armant.class, manager);
      assert whippet.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("destroy");
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithoutDestructorAndRemoveMethodWithoutParamsHasNoWebBeansRemoveMethod()
   {
      EnterpriseBeanModel<Laika> laika = Util.createEnterpriseBeanModel(Laika.class, manager);
      assert laika.getRemoveMethod() == null;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithMultipleDestructorAnnotationsFail()
   {
      EnterpriseBeanModel<Whippet> whippet = Util.createEnterpriseBeanModel(Whippet.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithDestructorAnnotationOnMethodNotAnEJBRemoveMethodFails()
   {
      EnterpriseBeanModel<GoldenRetriever> goldenRetriever = Util.createEnterpriseBeanModel(GoldenRetriever.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithDestructorWithDisposesParameterFails()
   {
      EnterpriseBeanModel<Pumi> pumi = Util.createEnterpriseBeanModel(Pumi.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithInitializerAnnotationOnRemoveMethodFails()
   {
      EnterpriseBeanModel<JackRussellTerrier> jackRussellTerrier = Util.createEnterpriseBeanModel(JackRussellTerrier.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithProducerAnnotationOnRemoveMethodFails()
   {
      EnterpriseBeanModel<RussellTerrier> russellTerrier = Util.createEnterpriseBeanModel(RussellTerrier.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithRemoveMethodTakingObserverAnnotatedParameterFails()
   {
      EnterpriseBeanModel<Toller> toller = Util.createEnterpriseBeanModel(Toller.class, manager);
   }

   @Test(expectedExceptions=DefinitionException.class)
   public void testStatelessEnterpriseBeansWithDestructorAnnotationFails() 
   {
      EnterpriseBeanModel<WelshCorgie> welshCorgie = Util.createEnterpriseBeanModel(WelshCorgie.class, manager);
   }
   
   
   
}
