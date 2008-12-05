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
import org.jboss.webbeans.test.ejb.model.invalid.GreatDane;
import org.jboss.webbeans.test.ejb.model.invalid.Greyhound;
import org.jboss.webbeans.test.ejb.model.invalid.Husky;
import org.jboss.webbeans.test.ejb.model.invalid.IrishTerrier;
import org.jboss.webbeans.test.ejb.model.invalid.Pekingese;
import org.jboss.webbeans.test.ejb.model.invalid.Pug;
import org.jboss.webbeans.test.ejb.model.valid.Laika;
import org.jboss.webbeans.test.ejb.model.valid.Pitbull;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

@SpecVersion("PDR")
@SuppressWarnings("unused")
public class EnterpriseBeanDeclarationTest extends AbstractTest
{

   @Test
   @SpecAssertion(section = "3.3")
   public void testStatelessWithDependentScopeOK()
   {
      EnterpriseBean<Giraffe> giraffe = BeanFactory.createEnterpriseBean(Giraffe.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithRequestScopeFails()
   {
      EnterpriseBean<Beagle> beagle = BeanFactory.createEnterpriseBean(Beagle.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithConversationScopeFails()
   {
      EnterpriseBean<Boxer> boxer = BeanFactory.createEnterpriseBean(Boxer.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithSessionScopeFails()
   {
      EnterpriseBean<Bullmastiff> boxer = BeanFactory.createEnterpriseBean(Bullmastiff.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithApplicationScopeFails()
   {
      EnterpriseBean<Dachshund> dachshund = BeanFactory.createEnterpriseBean(Dachshund.class);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithDependentScopeOK()
   {
      EnterpriseBean<GreatDane> greatDane = BeanFactory.createEnterpriseBean(GreatDane.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithRequestScopeFails()
   {
      EnterpriseBean<Greyhound> greyhound = BeanFactory.createEnterpriseBean(Greyhound.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithConversationScopeFails()
   {
      EnterpriseBean<Husky> husky = BeanFactory.createEnterpriseBean(Husky.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithSessionScopeFails()
   {
      EnterpriseBean<IrishTerrier> irishTerrier = BeanFactory.createEnterpriseBean(IrishTerrier.class);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithApplicationScopeOK()
   {
      EnterpriseBean<Laika> laika = BeanFactory.createEnterpriseBean(Laika.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanInterceptorFails()
   {
      EnterpriseBean<Pug> pug = BeanFactory.createEnterpriseBean(Pug.class);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanDecoratorFails()
   {
      EnterpriseBean<Pekingese> pekingese = BeanFactory.createEnterpriseBean(Pekingese.class);
   }

   @Test(expectedExceptions = DefinitionException.class, groups = {"enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3")
   public void testMultipleAnnotationDefinedEnterpriseBeansWithSameImplementationClassFails()
   {
      assert false;
   }

   @Test(groups = { "webbeansxml", "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3")
   public void testMultipleXMLDefinedEnterpriseBeansWithSameImplementationClassOK()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBean()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBeanComplementedWithXML()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "ejbjarxml", "stub"})
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBean()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "ejbjarxml", "stub"})
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBeanComplementedWithXML()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3.2")
   public void testAPITypesAreLocalInterfacesWithoutWildcardTypesOrTypeVariablesWithSuperInterfaces()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3.2")
   public void testEnterpriseBeanWithLocalViewAndParameterizedTypeIncludesBeanClassAndSuperclassesInAPITypes()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testObjectIsInAPITypes()
   {
      EnterpriseBean<Laika> laika = BeanFactory.createEnterpriseBean(Laika.class);
      assert laika.getTypes().contains(Object.class);
   }

   @Test(groups={"enterpriseBeans", "stub"})
   @SpecAssertion(section = "3.3.2")
   public void testRemoteInterfacesAreNotInAPITypes()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "producerMethod", "observerMethod", "removeMethod", "webbeansxml", "stub"})
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedEnterpriseBeanIgnoresProducerAndDisposalAndObserverAnnotations()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class, groups={"enterpriseBeans", "ejbjarxml", "singletons", "stub"})
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedSingletonsFail()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class, groups="enterpriseBeans")
   @SpecAssertion(section = "3.3")
   public void testMessageDrivenBeansNotOK()
   {
      EnterpriseBean<Leopard> Leopard = BeanFactory.createEnterpriseBean(Leopard.class);
   }

   
   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.7")
   public void testDefaultName()
   {
      EnterpriseBean<Pitbull> pitbull = BeanFactory.createEnterpriseBean(Pitbull.class);
      assert pitbull.getName().equals("pitbull");
   }
   
}
