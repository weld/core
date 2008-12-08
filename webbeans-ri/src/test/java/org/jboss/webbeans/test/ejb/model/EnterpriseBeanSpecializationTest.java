package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.ejb.model.invalid.GreaterDane;
import org.jboss.webbeans.test.ejb.model.valid.Hound;
import org.jboss.webbeans.test.ejb.model.valid.HoundOfBaskerville;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

@SpecVersion("20081206")
@SuppressWarnings("unused")
public class EnterpriseBeanSpecializationTest extends AbstractTest
{
   /**
    * If an implementation class of an enterprise Web Bean X defined using
    * annotations is annotated @Specializes, then the implementation class of X
    * must directly extend the implementation class of another enterprise Web
    * Bean Y defined using annotations. If the implementation class of X does
    * not directly extend the implementation class of another enterprise Web
    * Bean, a DefinitionException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "specialization", "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3.6")
   public void testAnnotationDefinedSpecializingEnterpriseBeanMustDirectlyExtendAnotherAnnotationDefinedEnterpriseBean()
   {
      assert false;
   }

   /**
    * X inherits all binding types of Y
    */
   @Test(groups = { "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingBeanInheritsBindingTypes()
   {
      EnterpriseBean<Hound> hound = BeanFactory.createEnterpriseBean(Hound.class);
      EnterpriseBean<HoundOfBaskerville> houndOfBaskerville = BeanFactory.createEnterpriseBean(HoundOfBaskerville.class);
      assert compareBindingTypesOK(hound, houndOfBaskerville);
   }

   private boolean compareBindingTypesOK(EnterpriseBean<Hound> hound, EnterpriseBean<HoundOfBaskerville> houndOfBaskerville)
   {
      if (hound.getBindingTypes().size() != houndOfBaskerville.getBindingTypes().size())
      {
         return false;
      }
      if (!hound.getBindingTypes().containsAll(houndOfBaskerville.getBindingTypes()))
      {
         return false;
      }
      if (!houndOfBaskerville.getBindingTypes().containsAll(hound.getBindingTypes()))
      {
         return false;
      }
      return true;
   }

   /**
    * if Y has a name, X has the same name as Y.
    */
   @Test(groups = { "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingBeanInheritsNameIfAny()
   {
      EnterpriseBean<HoundOfBaskerville> houndOfBaskerville = BeanFactory.createEnterpriseBean(HoundOfBaskerville.class);
      assert houndOfBaskerville.getName().equals("Pongo");
   }

   /**
    * X must support all local interfaces supported by Y. Otherwise, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time.
    */
   @Test(expectedExceptions = DefinitionException.class, groups = { "stub", "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalInterfacesOfSpecializedFails()
   {
      assert false;
   }

   /**
    * if Y supports a bean-class local view, X must also support a bean-class
    * local view. Otherwise, a DefinitionException is thrown by the Web Bean
    * manager at initialization time.
    */
   @Test(expectedExceptions = DefinitionException.class, groups = { "stub", "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalViewOfSpecializedFails()
   {
      assert false;
   }

   /**
    * We say that X directly specializes Y, and we can be certain that Y will
    * never be instantiated or called by the Web Bean manager if X is enabled.
    */
   @Test(expectedExceptions = DefinitionException.class, groups = { "stub", "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void EnabledSpecializationOverridesSpecialized()
   {
      assert false;
   }

   @Test(groups = { "stub", "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializationOnAnnotationDefinedBean()
   {
      assert false;
   }

   @Test(expectedExceptions = DeploymentException.class, groups = { "stub", "enterpriseBeans", "specialization" })
   @SpecAssertion(section = "3.3")
   public void testMultipleEnabledSpecializedEnterpriseBeanFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class, groups = { "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testAnnotationDefinedSpecializingEnterpriseBeanNotDirectlyExtendingAnnotationDefinedEnterpriseBeanFails()
   {
      EnterpriseBean<GreaterDane> greaterDane = BeanFactory.createEnterpriseBean(GreaterDane.class);
   }

   @Test(expectedExceptions = DefinitionException.class, groups = { "stub", "specialization", "enterpriseBeans" })
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializingEnterpriseBeanNotImplementingAnnotationDefinedEnterpriseBeanFails()
   {
      assert false;
   }

}
