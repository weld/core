package org.jboss.weld.util.log;

import java.util.Locale;

import ch.qos.cal10n.IMessageConveyor;

public interface IMessageConveyerFactory
{

   /**
    * Get the message conveyer for the default locale.
    * 
    * By default, Locale.getDefault() will be used as the locale, but a custom
    * implementation of MessageConveyerFactory may choose to use an alternative
    * locale.
    * 
    */
   public abstract IMessageConveyor getDefaultMessageConveyer();

   /**
    * Get the message conveyer for the given locale.
    * 
    */
   public abstract IMessageConveyor getMessageConveyer(Locale locale);

}