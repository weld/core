package org.jboss.weld.util.log;

import java.util.Locale;

import org.jboss.weld.util.serviceProvider.ServiceLoader;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;

/**
 * A factory for message conveyers, that looks for service providers which will
 * convey a message.
 * 
 * If none are found, the default {@link MessageConveyor} is used.
 * 
 * @author pmuir
 * 
 */
public class MessageConveyerFactoryLoader
{
   
   private static IMessageConveyerFactory load()
   {
      ServiceLoader<IMessageConveyerFactory> serviceLoader = ServiceLoader.load(IMessageConveyerFactory.class);
      int i = 0;
      for (IMessageConveyerFactory f : serviceLoader)
      {
         if (i > 0)
         {
            throw new IllegalStateException("Maximum one service provider for IMessageConveyerFactory allowed, got " + serviceLoader);
         }
         return f;
      }
      return new DefaultMessageConveyerFactory();
   }

   private static class DefaultMessageConveyerFactory implements IMessageConveyerFactory
   {
      private final IMessageConveyor defaultMessageConveyer;

      public DefaultMessageConveyerFactory()
      {
         this.defaultMessageConveyer = new MessageConveyor(Locale.ENGLISH);
      }

      public IMessageConveyor getDefaultMessageConveyer()
      {
         return defaultMessageConveyer;
      }

      public IMessageConveyor getMessageConveyer(Locale locale)
      {
         return new MessageConveyor(locale);
      }

   }
   
   private IMessageConveyerFactory factory;
   
   public MessageConveyerFactoryLoader() 
   {
      this.factory = load();
   }
   
   public IMessageConveyerFactory getMessageConveyerFactory()
   {
      return factory;
   }

}
