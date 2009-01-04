package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.SessionScoped;

@SessionScoped
public class Raisio implements CityInterface, Serializable
{
   public void foo()
   {
   }
}
