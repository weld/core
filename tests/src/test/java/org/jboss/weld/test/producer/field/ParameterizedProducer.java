package org.jboss.weld.test.producer.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Produces;

public class ParameterizedProducer
{

   @Produces
   public List<String> createStringList()
   {
      return Arrays.asList("aaa", "bbb");
   }

   @Produces
   public ArrayList<Integer> createIntegerList()
   {
      List<Integer> list = Arrays.asList(1, 2, 3, 4);
      ArrayList<Integer> arrayList = new ArrayList<Integer>();
      arrayList.addAll(list);
      return arrayList;
   }
}
