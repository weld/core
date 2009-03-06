package org.jboss.webbeans.test.unit.implementation;

import javax.ejb.Local;
import javax.ejb.Remove;

@Local
public interface BadLocal
{
   @Remove
   public void bye();
}
