package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Produces;
import javax.webbeans.SessionScoped;

@SessionScoped
public class ScopeTest implements Serializable
{
   public ScopeTest()
   {
   }

   @Produces
   public Violation makeViolation()
   {
      return new Violation();
   }
}
