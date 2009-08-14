package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;

public class ListInstance
{
   @Any
   Instance<List> instance;
   
   public Instance<List> get()
   {
      return instance;
   }

}
