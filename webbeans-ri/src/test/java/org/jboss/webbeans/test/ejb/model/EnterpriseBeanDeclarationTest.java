package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.beans.Giraffe;
import org.jboss.webbeans.test.beans.Leopard;
import org.jboss.webbeans.test.ejb.model.invalid.Beagle;
import org.jboss.webbeans.test.ejb.model.invalid.Boxer;
import org.jboss.webbeans.test.ejb.model.invalid.Bullmastiff;
import org.jboss.webbeans.test.ejb.model.invalid.Dachshund;
import org.jboss.webbeans.test.ejb.model.invalid.Greyhound;
import org.jboss.webbeans.test.ejb.model.invalid.Husky;
import org.jboss.webbeans.test.ejb.model.invalid.IrishTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.Pekingese;
import org.jboss.webbeans.test.ejb.model.invalid.Pug;
import org.jboss.webbeans.test.ejb.model.valid.GreatDane;
import org.jboss.webbeans.test.ejb.model.valid.Laika;
import org.jboss.webbeans.test.ejb.model.valid.Pitbull;
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

@SpecVersion("PDR")
@SuppressWarnings("unused")
public class EnterpriseBeanDeclarationTest extends AbstractTest
{

   @Test
   @SpecAssertion(section = "3.3")
   public void testStatelessWithDependentScopeOK()
   {
      EnterpriseBeanModel<Giraffe> giraffe = Util.createEnterpriseBeanModel(Giraffe.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithRequestScopeFails()
   {
      EnterpriseBeanModel<Beagle> beagle = Util.createEnterpriseBeanModel(Beagle.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithConversationScopeFails()
   {
      EnterpriseBeanModel<Boxer> boxer = Util.createEnterpriseBeanModel(Boxer.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithSessionScopeFails()
   {
      EnterpriseBeanModel<Bullmastiff> boxer = Util.createEnterpriseBeanModel(Bullmastiff.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithApplicationScopeFails()
   {
      EnterpriseBeanModel<Dachshund> dachshund = Util.createEnterpriseBeanModel(Dachshund.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithDependentScopeOK()
   {
      EnterpriseBeanModel<GreatDane> greatDane = Util.createEnterpriseBeanModel(GreatDane.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithRequestScopeFails()
   {
      EnterpriseBeanModel<Greyhound> greyhound = Util.createEnterpriseBeanModel(Greyhound.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithConversationScopeFails()
   {
      EnterpriseBeanModel<Husky> husky = Util.createEnterpriseBeanModel(Husky.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithSessionScopeFails()
   {
      EnterpriseBeanModel<IrishTerrier> irishTerrier = Util.createEnterpriseBeanModel(IrishTerrier.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithApplicationScopeOK()
   {
      EnterpriseBeanModel<Laika> laika = Util.createEnterpriseBeanModel(Laika.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanInterceptorFails()
   {
      EnterpriseBeanModel<Pug> pug = Util.createEnterpriseBeanModel(Pug.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanDecoratorFails()
   {
      EnterpriseBeanModel<Pekingese> pekingese = Util.createEnterpriseBeanModel(Pekingese.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class, groups = "enterpriseBeans")
   @SpecAssertion(section = "3.3")
   public void testMultipleAnnotationDefinedEnterpriseBeansWithSameImplementationClassFails()
   {
      assert false;
   }

   @Test(groups = { "webbeansxml", "enterpriseBeans" })
   @SpecAssertion(section = "3.3")
   public void testMultipleXMLDefinedEnterpriseBeansWithSameImplementationClassOK()
   {
      assert false;
   }




   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBean()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBeanComplementedWithXML()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "ejbjarxml"})
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBean()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "ejbjarxml"})
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBeanComplementedWithXML()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testAPITypesAreLocalInterfacesWithoutWildcardTypesOrTypeVariablesWithSuperInterfaces()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testEnterpriseBeanWithLocalViewAndParameterizedTypeIncludesBeanClassAndSuperclassesInAPITypes()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.2")
   public void testObjectIsInAPITypes()
   {
      EnterpriseBeanModel<Laika> laika = Util.createEnterpriseBeanModel(Laika.class, manager);
      assert laika.getApiTypes().contains(Object.class);
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testRemoteInterfacesAreNotInAPITypes()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "producerMethod", "observerMethod", "renoveMethod", "webbeansxml"})
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedEnterpriseBeanIgnoresProducerAndDisposalAndObserverAnnotations()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class, groups={"enterpriseBeans", "ejbjarxml", "singletons"})
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedSingletonsFail()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testMessageDrivenBeansNotOK()
   {
      EnterpriseBeanModel<Leopard> Leopard = Util.createEnterpriseBeanModel(Leopard.class, manager);
   }

   
   @Test
   @SpecAssertion(section = "3.3.7")
   public void testDefaultName()
   {
      EnterpriseBeanModel<Pitbull> pitbull = Util.createEnterpriseBeanModel(Pitbull.class, manager);
      assert pitbull.getName().equals("pitbull");
   }
   
}
