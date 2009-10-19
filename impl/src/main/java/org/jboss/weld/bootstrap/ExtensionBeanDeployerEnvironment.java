package org.jboss.weld.bootstrap;

import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.ejb.EjbDescriptors;

public class ExtensionBeanDeployerEnvironment extends BeanDeployerEnvironment
{
   
   private final Set<ExtensionBean> extensionBeans;
   

   public ExtensionBeanDeployerEnvironment(EjbDescriptors ejbDescriptors, BeanManagerImpl manager)
   {
      super(ejbDescriptors, manager);
      this.extensionBeans = new HashSet<ExtensionBean>();
   }
   
   @Override
   public Set<ExtensionBean> getBeans()
   {
      return extensionBeans;
   }
   
   @Override
   public void addExtension(ExtensionBean bean)
   {
      extensionBeans.add(bean);
   }

}
