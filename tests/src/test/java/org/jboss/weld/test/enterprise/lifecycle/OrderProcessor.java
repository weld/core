package org.jboss.weld.test.enterprise.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class OrderProcessor
{   
   public static boolean postConstructCalled = false;
   
   public static boolean preDestroyCalled = true;
   
   @PostConstruct
   public void postConstruct()
   {
      postConstructCalled = true;
   }
   
   @PreDestroy
   public void preDestroy()
   {
      preDestroyCalled = true;
   }
   
   public void order()
   {
      
   }
    
   
}
