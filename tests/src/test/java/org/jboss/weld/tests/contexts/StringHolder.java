package org.jboss.weld.tests.contexts;

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
