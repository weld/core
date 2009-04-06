package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.inject.Instance;
import javax.inject.Obtains;

public class ListInstance
{
   @Obtains
   Instance<List> instance;
   
   public List get()
   {
      return instance.get();
   }

}
