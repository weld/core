package org.jboss.weld.test.producer.field;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class Target
{
   
   @Inject private Collection<String> strings;
   
   @Inject private Collection<Integer> integers;
   
   @Inject private List<String> stringList;
   
   public Collection<String> getStrings()
   {
      return strings;
   }
   
   public Collection<Integer> getIntegers()
   {
      return integers;
   }
   
   public List<String> getStringList()
   {
      return stringList;
   }

}
