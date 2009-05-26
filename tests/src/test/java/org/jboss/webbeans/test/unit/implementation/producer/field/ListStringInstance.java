package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.inject.Obtains;

public class ListStringInstance
{
   @Obtains Instance<List<String>> instance;

   public List<String> get()
   {
      return instance.get();
   }

}
