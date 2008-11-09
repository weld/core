package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;

public class XmlSimpleBean<T> extends SimpleBean<T>
{

   public XmlSimpleBean(Class<T> type, ManagerImpl manager)
   {
      super(type, manager);
   }
   
   protected boolean isDefinedInXml()
   {
      return true;
   }
}
