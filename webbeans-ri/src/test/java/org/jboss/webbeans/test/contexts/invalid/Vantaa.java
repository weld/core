package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.SessionScoped;

@SessionScoped
public class Vantaa implements Serializable
{
   @Current
   private Violation reference;

   public Violation getReference()
   {
      return reference;
   }

   public void setReference(Violation reference)
   {
      this.reference = reference;
   }

   public String test() {
      return reference.toString();
   }
   
}
