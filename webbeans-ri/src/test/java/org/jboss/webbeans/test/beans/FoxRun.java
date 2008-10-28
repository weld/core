package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;

public class FoxRun
{
   
   @Current
   public Fox fox;
   
   @Current
   public Fox anotherFox;
   
}
