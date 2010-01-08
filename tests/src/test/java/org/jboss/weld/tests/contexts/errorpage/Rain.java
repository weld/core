package org.jboss.weld.tests.contexts.errorpage;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Rain
{
   public String getSeverityLevel()
   {
      return "medium";
   }
}
