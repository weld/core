package org.jboss.webbeans.test.unit.context;

import java.util.List;

import javax.enterprise.inject.Current;


public class StringHolder
{

   @Current 
   private List<String> strings;
   
   public List<String> getStrings()
   {
      return strings;
   }
   
}
