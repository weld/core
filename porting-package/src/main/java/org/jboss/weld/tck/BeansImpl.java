package org.jboss.weld.tck;

import org.jboss.jsr299.tck.spi.Beans;
import org.jboss.weld.util.Proxies;

/**
 * Implements the Beans SPI for the TCK specifically for the JBoss RI.
 * 
 * @author Shane Bryzak
 * @author Pete Muir
 * @author David Allen
 * 
 */
public class BeansImpl implements Beans
{

   public boolean isProxy(Object instance)
   {
      return Proxies.isProxy(instance);
   }

}
