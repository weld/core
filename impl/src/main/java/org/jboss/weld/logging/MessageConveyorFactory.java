package org.jboss.weld.logging;

import java.util.Locale;

import org.jboss.weld.util.serviceProvider.DefaultServiceLoader;

import ch.qos.cal10n.IMessageConveyor;

public abstract class MessageConveyorFactory
{
   
   private static MessageConveyorFactory INSTANCE = load();
   
   private static MessageConveyorFactory load()
   {
      DefaultServiceLoader<MessageConveyorFactory> serviceLoader = DefaultServiceLoader.load(MessageConveyorFactory.class);
      int i = 0;
      for (MessageConveyorFactory f : serviceLoader)
      {
         if (i > 0)
         {
            throw new IllegalStateException("Maximum one service provider for IMessageConveyerFactory allowed, got " + serviceLoader);
         }
         return f;
      }
      // Return the default
      return new WeldMessageConveyerFactory();
   }
   
   public static MessageConveyorFactory messageConveyerFactory()
   {
      return INSTANCE;
   }
   
   public static IMessageConveyor defaultMessageConveyer(String subsystem)
   {
      return messageConveyerFactory().getDefaultMessageConveyer(subsystem);
   }

   /**
    * Get the message conveyer for the default locale.
    * 
    * By default, Locale.getDefault() will be used as the locale, but a custom
    * implementation of MessageConveyerFactory may choose to use an alternative
    * locale.
    * 
    */
   public abstract IMessageConveyor getDefaultMessageConveyer(String subsystem);

   /**
    * Get the message conveyer for the given locale.
    * 
    */
   public abstract IMessageConveyor getMessageConveyer(Locale locale, String subsystem);

}