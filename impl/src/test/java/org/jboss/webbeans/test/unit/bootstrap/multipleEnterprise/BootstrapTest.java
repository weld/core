package org.jboss.webbeans.test.unit.bootstrap.multipleEnterprise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.manager.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class BootstrapTest extends AbstractWebBeansTest
{
   
   @Test(groups="bootstrap")
   public void testMultipleEnterpriseBean()
   {
      List<Bean<?>> beans = manager.getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(Hound.class);
      assert classes.containsKey(Elephant.class);
      assert classes.containsKey(Panther.class);
      assert classes.containsKey(Tiger.class);
      
      assert classes.get(Hound.class) instanceof EnterpriseBean;
      assert classes.get(Elephant.class) instanceof EnterpriseBean;
      assert classes.get(Panther.class) instanceof EnterpriseBean;
      assert classes.get(Tiger.class) instanceof EnterpriseBean;
   }
   
}
