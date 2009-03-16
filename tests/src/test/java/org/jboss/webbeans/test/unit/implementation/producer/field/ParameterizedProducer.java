package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Arrays;
import java.util.List;

import javax.inject.Produces;

public class ParameterizedProducer
{

   @Produces
   public List<String> create()
   {
      return Arrays.asList("aaa", "bbb");
   }

}
