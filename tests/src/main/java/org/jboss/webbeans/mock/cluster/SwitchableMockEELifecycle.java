package org.jboss.webbeans.mock.cluster;

import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.mock.MockEELifecycle;

public class SwitchableMockEELifecycle extends MockEELifecycle
{
   
   private final Map<Integer, BeanStore> requestBeanStores;
   private final Map<Integer, BeanStore> sessionBeanStores;
   private final Map<Integer, BeanStore> applicationBeanStores;
   
   private int id = 1;
   
   public SwitchableMockEELifecycle()
   {
      this.requestBeanStores = new HashMap<Integer, BeanStore>();
      this.sessionBeanStores = new HashMap<Integer, BeanStore>();
      this.applicationBeanStores = new HashMap<Integer, BeanStore>();
   }
   
   @Override
   protected BeanStore getRequestBeanStore()
   {
      return requestBeanStores.get(id);
   }
   
   @Override
   protected BeanStore getSessionBeanStore()
   {
      return sessionBeanStores.get(id);
   }
   
   @Override
   protected BeanStore getApplicationBeanStore()
   {
      return applicationBeanStores.get(id);
   }
   
   public void use(int id)
   {
      this.id = id;
   }

}
