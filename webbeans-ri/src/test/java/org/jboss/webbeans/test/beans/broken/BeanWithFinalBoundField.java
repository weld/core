package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Current;

import org.jboss.webbeans.test.beans.Tuna;

public class BeanWithFinalBoundField
{
   
   @Current
   public final Tuna tuna = null;
   
}
