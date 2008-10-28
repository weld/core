package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Current;

import org.jboss.webbeans.test.beans.Tuna;

public class BeanWithStaticBoundField
{
   
   @Current
   public static Tuna tuna;
   
}
