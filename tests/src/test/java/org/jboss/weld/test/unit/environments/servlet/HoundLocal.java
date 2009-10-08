package org.jboss.weld.test.unit.environments.servlet;

import javax.ejb.Local;

@Local
public interface HoundLocal
{
   
   public void ping();
   
}
