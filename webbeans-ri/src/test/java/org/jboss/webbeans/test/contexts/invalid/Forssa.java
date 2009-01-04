package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.SessionScoped;

@SessionScoped
public class Forssa implements Serializable
{
   public Forssa() {
   }
   
   @Initializer
   public Forssa(@Current Violation reference) {
   }
   
}
