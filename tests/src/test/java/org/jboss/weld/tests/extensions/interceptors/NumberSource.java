package org.jboss.weld.tests.extensions.interceptors;

/**
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class NumberSource
{
   @Incremented
   public int value()
   {
      return 1;
   }
}
