package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.SessionScoped;

@SessionScoped
public class Kotka implements Serializable
{
   @CityBinding
   public void foo() {
      
   }
}
