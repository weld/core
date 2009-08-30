package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class ListInstance
{
   @Inject @Any
   Instance<List> instance;
   
   public Instance<List> get()
   {
      return instance;
   }

}
