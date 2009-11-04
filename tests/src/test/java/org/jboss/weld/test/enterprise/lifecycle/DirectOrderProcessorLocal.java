package org.jboss.weld.test.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface DirectOrderProcessorLocal
{
   void order();
}
