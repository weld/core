package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;

public class ListStringInstance
{
   @Any Instance<List<String>> instance;

   public List<String> get()
   {
      return instance.get();
   }

}
