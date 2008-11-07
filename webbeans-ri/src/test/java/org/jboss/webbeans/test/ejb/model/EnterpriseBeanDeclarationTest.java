package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.bean.EnterpriseBean;
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
      EnterpriseBean<Giraffe> giraffe = Util.createEnterpriseBean(Giraffe.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithRequestScopeFails()
   {
      EnterpriseBean<Beagle> beagle = Util.createEnterpriseBean(Beagle.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithConversationScopeFails()
   {
      EnterpriseBean<Boxer> boxer = Util.createEnterpriseBean(Boxer.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithSessionScopeFails()
   {
      EnterpriseBean<Bullmastiff> boxer = Util.createEnterpriseBean(Bullmastiff.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithApplicationScopeFails()
   {
      EnterpriseBean<Dachshund> dachshund = Util.createEnterpriseBean(Dachshund.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithDependentScopeOK()
   {
      EnterpriseBean<GreatDane> greatDane = Util.createEnterpriseBean(GreatDane.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithRequestScopeFails()
   {
      EnterpriseBean<Greyhound> greyhound = Util.createEnterpriseBean(Greyhound.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithConversationScopeFails()
   {
      EnterpriseBean<Husky> husky = Util.createEnterpriseBean(Husky.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithSessionScopeFails()
   {
      EnterpriseBean<IrishTerrier> irishTerrier = Util.createEnterpriseBean(IrishTerrier.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithApplicationScopeOK()
   {
      EnterpriseBean<Laika> laika = Util.createEnterpriseBean(Laika.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanInterceptorFails()
   {
      EnterpriseBean<Pug> pug = Util.createEnterpriseBean(Pug.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanDecoratorFails()
   {
      EnterpriseBean<Pekingese> pekingese = Util.createEnterpriseBean(Pekingese.class, manager);
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
      EnterpriseBean<Laika> laika = Util.createEnterpriseBean(Laika.class, manager);
      assert laika.getTypes().contains(Object.class);
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
      EnterpriseBean<Leopard> Leopard = Util.createEnterpriseBean(Leopard.class, manager);
   }

   
   @Test
   @SpecAssertion(section = "3.3.7")
   public void testDefaultName()
   {
      EnterpriseBean<Pitbull> pitbull = Util.createEnterpriseBean(Pitbull.class, manager);
      assert pitbull.getName().equals("pitbull");
   }
   
}
