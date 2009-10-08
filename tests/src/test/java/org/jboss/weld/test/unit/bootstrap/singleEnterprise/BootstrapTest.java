package org.jboss.weld.test.unit.bootstrap.singleEnterprise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class BootstrapTest extends AbstractWebBeansTest
{
   
   @Test(groups="bootstrap")
   public void testSingleEnterpriseBean()
   {
      List<Bean<?>> beans = getCurrentManager().getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(Hound.class);
   }
   
}
