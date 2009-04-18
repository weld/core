package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface DirectOrderProcessorLocal
{
   void order();
}
