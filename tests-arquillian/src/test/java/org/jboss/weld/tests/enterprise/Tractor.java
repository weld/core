package org.jboss.weld.tests.enterprise;

import javax.ejb.Remote;

@Remote
public interface Tractor
{

   public void observeRefuel(Fuel fuel);

   public Fumes smoke();

   public void carbonCaptureDevice(Fumes fumes);

}