package org.jboss.weld.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface DirectOrderProcessorLocal
{
   void order();
}
