package org.jboss.weld.tests.unit.environments.servlet;

import javax.ejb.Local;

@Local
public interface HoundLocal
{
   
   public void ping();
   
}
