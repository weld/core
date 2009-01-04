package org.jboss.webbeans.test.contexts.invalid;

import java.io.Serializable;

import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.SessionScoped;

@SessionScoped
public class Pori implements Serializable
{
   public Pori() {
   }
   
   @Initializer
   public Pori(@Current Violation reference) {
   }
}
