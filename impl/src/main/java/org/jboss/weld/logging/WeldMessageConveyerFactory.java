/**
 * 
 */
package org.jboss.weld.logging;

import java.util.Locale;

import ch.qos.cal10n.IMessageConveyor;

class WeldMessageConveyerFactory extends MessageConveyorFactory
{

   @Override
   public IMessageConveyor getDefaultMessageConveyer(String subsystem)
   {
      return new WeldMessageConveyor(Locale.ENGLISH, subsystem);
   }

   @Override
   public IMessageConveyor getMessageConveyer(Locale locale, String subsystem)
   {
      return new WeldMessageConveyor(locale, subsystem);
   }

}