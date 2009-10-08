package org.jboss.weld.context;

import javax.inject.Singleton;

public class SingletonContext extends AbstractApplicationContext
{
   
   public SingletonContext()
   {
      super(Singleton.class);
   }

}
