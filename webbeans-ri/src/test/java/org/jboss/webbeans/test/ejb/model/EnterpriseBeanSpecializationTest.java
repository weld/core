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
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

@SpecVersion("PDR")
@SuppressWarnings("unused")
public class EnterpriseBeanSpecializationTest extends AbstractTest
{
   @Test(groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingBeanInheritsBindingTypes()
   {
      EnterpriseBean<Hound> hound = Util.createEnterpriseBean(Hound.class, manager);
      EnterpriseBean<HoundOfBaskerville> houndOfBaskerville = Util.createEnterpriseBean(HoundOfBaskerville.class, manager);
      assert compareBindingTypesOK(hound, houndOfBaskerville);
   }

   private boolean compareBindingTypesOK(EnterpriseBean<Hound> hound,
         EnterpriseBean<HoundOfBaskerville> houndOfBaskerville)
   {
      if (hound.getBindingTypes().size() != houndOfBaskerville.getBindingTypes().size()) {
         return false;
      }
      if (!hound.getBindingTypes().containsAll(houndOfBaskerville.getBindingTypes())) {
         return false;
      }
      if (!houndOfBaskerville.getBindingTypes().containsAll(hound.getBindingTypes())) {
         return false;
      }
      return true;
   }

   @Test(groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingBeanInheritsNameIfAny()
   {
      EnterpriseBean<HoundOfBaskerville> houndOfBaskerville = Util.createEnterpriseBean(HoundOfBaskerville.class, manager);
      assert houndOfBaskerville.getName().equals("Pongo");
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalInterfacesOfSpecializedFails()
   {
      assert false;
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalViewOfSpecializedFails()
   {
      assert false;
   }

   @Test(groups={"stub", "specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializationOnAnnotationDefinedBean()
   {
      assert false;
   }

   
   @Test(expectedExceptions = DeploymentException.class, groups={"stub", "enterpriseBeans", "specialization"})
   @SpecAssertion(section = "3.3")
   public void testMultipleEnabledSpecializedEnterpriseBeanFails()
   {
      assert false;
   }
      
   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testAnnotationDefinedSpecializingEnterpriseBeanNotDirectlyExtendingAnnotationDefinedEnterpriseBeanFails()
   {
      EnterpriseBean<GreaterDane> greaterDane = Util.createEnterpriseBean(GreaterDane.class, manager);
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializingEnterpriseBeanNotImplementingAnnotationDefinedEnterpriseBeanFails()
   {
     assert false;
   }
   
}
