package org.jboss.weld.test.producer.field;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class ListStringInstance
{
   @Inject @Any Instance<List<String>> instance;

   public List<String> get()
   {
      return instance.get();
   }

}
