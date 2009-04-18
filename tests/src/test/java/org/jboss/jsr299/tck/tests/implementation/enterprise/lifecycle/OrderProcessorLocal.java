package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface OrderProcessorLocal
{
   void order();
}
