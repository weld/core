package org.jboss.weld.test.unit.context;

import java.util.List;

import javax.inject.Inject;


public class StringHolder
{

   @Inject 
   private List<String> strings;
   
   public List<String> getStrings()
   {
      return strings;
   }
   
}
