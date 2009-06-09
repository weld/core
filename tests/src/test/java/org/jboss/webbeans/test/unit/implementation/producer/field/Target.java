package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Current;

public class Target
{
   
   @Current private Collection<String> strings;
   
   @Current private Collection<Integer> integers;
   
   @Current private List<String> stringList;
   
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
