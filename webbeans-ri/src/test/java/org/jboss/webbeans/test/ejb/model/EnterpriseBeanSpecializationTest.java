package org.jboss.webbeans.test.ejb.model;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;

import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.ejb.model.valid.GreaterDane;
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
      EnterpriseBeanModel<Hound> hound = Util.createEnterpriseBeanModel(Hound.class, manager);
      EnterpriseBeanModel<HoundOfBaskerville> houndOfBaskerville = Util.createEnterpriseBeanModel(HoundOfBaskerville.class, manager);
      assert compareBindingTypesOK(hound, houndOfBaskerville);
   }

   private boolean compareBindingTypesOK(EnterpriseBeanModel<Hound> hound,
         EnterpriseBeanModel<HoundOfBaskerville> houndOfBaskerville)
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
      EnterpriseBeanModel<HoundOfBaskerville> houndOfBaskerville = Util.createEnterpriseBeanModel(HoundOfBaskerville.class, manager);
      assert houndOfBaskerville.getName().equals("Pongo");
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalInterfacesOfSpecializedFails()
   {
      assert false;
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalViewOfSpecializedFails()
   {
      assert false;
   }

   @Test(groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializationOnAnnotationDefinedBean()
   {
      assert false;
   }

   
   @Test(expectedExceptions = DeploymentException.class, groups={"enterpriseBeans", "specialization"})
   @SpecAssertion(section = "3.3")
   public void testMultipleEnabledSpecializedEnterpriseBeanFails()
   {
      assert false;
   }
      
   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testAnnotationDefinedSpecializingEnterpriseBeanNotDirectlyExtendingAnnotationDefinedEnterpriseBeanFails()
   {
      EnterpriseBeanModel<GreaterDane> greaterDane = Util.createEnterpriseBeanModel(GreaterDane.class, manager);
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializingEnterpriseBeanNotImplementingAnnotationDefinedEnterpriseBeanFails()
   {
     assert false;
   }
   
}
