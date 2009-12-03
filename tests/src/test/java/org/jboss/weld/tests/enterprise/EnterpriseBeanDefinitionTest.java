package org.jboss.weld.tests.enterprise;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class EnterpriseBeanDefinitionTest extends AbstractWeldTest
{
   
   @Test(description="WELD-305")
   public void testSuperInterfacesAreBeanTypes()
   {
      Bean<?> bean = getBean(Dog.class);
      assert typeSetMatches(bean.getTypes(), Object.class, Dog.class, Animal.class);
   }
   
}
