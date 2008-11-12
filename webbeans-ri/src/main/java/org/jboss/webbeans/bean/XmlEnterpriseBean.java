package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;

public class XmlEnterpriseBean<T> extends EnterpriseBean<T>
{

   public XmlEnterpriseBean(Class<T> type, ManagerImpl manager)
   {
      super(type, manager);
   }
   
   protected boolean isDefinedInXml()
   {
      return true;
   }
}
