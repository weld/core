package org.jboss.webbeans.test.unit.context;

import java.util.Arrays;
import java.util.List;

import javax.context.RequestScoped;
import javax.inject.Produces;

public class ParameterizedTypeScoped
{

   @RequestScoped
   @Produces
   public List<String> create()
   {
      return Arrays.asList("iemon", "houjitya");
   }
}
