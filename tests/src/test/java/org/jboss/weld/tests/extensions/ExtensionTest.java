package org.jboss.weld.tests.extensions;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
@Extension("javax.enterprise.inject.spi.Extension")
public class ExtensionTest extends AbstractWeldTest
{
   
   @Test(description="WELD-234")
   public void testExtensionInjectableAsBean()
   {
      assert SimpleExtension.getInstance() != null;
      assert getCurrentManager().getInstanceByType(SimpleExtension.class).equals(SimpleExtension.getInstance());
   }
   
   @Test(description="WELD-243")
   public void testContainerEventsOnlySentToExtensionBeans()
   {
      ExtensionObserver extensionObserver = getCurrentManager().getInstanceByType(ExtensionObserver.class);
      OtherObserver otherObserver = getCurrentManager().getInstanceByType(OtherObserver.class);
      
      assert extensionObserver.isBeforeBeanDiscovery();
      assert extensionObserver.isAllBeforeBeanDiscovery();
      assert !otherObserver.isBeforeBeanDiscovery();
      assert !otherObserver.isAllBeforeBeanDiscovery();
      
      assert extensionObserver.isAfterBeanDiscovery();
      assert extensionObserver.isAllAfterBeanDiscovery();
      assert !otherObserver.isAfterBeanDiscovery();
      assert !otherObserver.isAllAfterBeanDiscovery();
      
      assert extensionObserver.isProcessAnnotatedType();
      assert extensionObserver.isAllProcessAnnnotatedType();
      assert !otherObserver.isProcessAnnotatedType();
      assert !otherObserver.isAllProcessAnnotatedType();
      
      assert extensionObserver.isProcessBean();
      assert extensionObserver.isAllProcessBean();
      assert !otherObserver.isProcessBean();
      assert !otherObserver.isAllProcessBean();
      
      assert extensionObserver.isProcessInjectionTarget();
      assert extensionObserver.isAllProcessInjectionTarget();
      assert !otherObserver.isProcessInjectionTarget();
      assert !otherObserver.isAllProcessInjectionTarget();
      
      assert extensionObserver.isProcessManagedBean();
      assert extensionObserver.isAllProcessManagedBean();
      assert !otherObserver.isProcessManagedBean();
      assert !otherObserver.isAllProcessManagedBean();
      
      assert extensionObserver.isProcessObserverMethod();
      assert extensionObserver.isAllProcessObserverMethod();
      assert !otherObserver.isProcessObserverMethod();
      assert !otherObserver.isAllProcessObserverMethod();
      
      assert extensionObserver.isProcessProducer();
      assert extensionObserver.isAllProcessProducer();
      assert !otherObserver.isProcessProducer();
      assert !otherObserver.isAllProcessProducer();
      
      assert extensionObserver.isProcessProducerField();
      assert extensionObserver.isAllProcessProducerField();
      assert !otherObserver.isProcessProducerField();
      assert !otherObserver.isAllProcessProducerField();
      
      assert extensionObserver.isProcessProducerMethod();
      assert extensionObserver.isAllProcessProducerField();
      assert !otherObserver.isProcessProducerMethod();
      assert !otherObserver.isAllProcessProducerMethod();
      
      assert extensionObserver.isProcessSessionBean();
      assert extensionObserver.isAllProcessSessionBean();
      assert !otherObserver.isProcessSessionBean();
      assert !otherObserver.isAllProcessSessionBean();
      
      assert extensionObserver.isAfterDeploymentValidation();
      assert extensionObserver.isAllAfterDeploymentValidation();
      assert !otherObserver.isAfterDeploymentValidation();
      assert !otherObserver.isAllAfterDeploymentValidation(); 
      
   }

}
