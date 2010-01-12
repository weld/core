package org.jboss.weld.logging;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.util.reflection.SecureReflections;

import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class WeldMessageConveyor extends MessageConveyor
{
   
   private static class ComputeMessagePrefix implements Function<Enum<?>, String>
   {
      
      private final String subsystem;

      private ComputeMessagePrefix(String subsystem)
      {
         this.subsystem = subsystem;
      }

      public String apply(Enum<?> from)
      {
         Field field;
         try
         {
            field = SecureReflections.getField(from.getClass(), from.name());
         }
         catch (NoSuchFieldException e)
         {
            throw new IllegalArgumentException("Cannot reflect on key to obtain @MessageId. Key: " + from + "; Key Type: " + from.getClass());
         }
         if (!field.isAnnotationPresent(MessageId.class))
         {
            throw new IllegalArgumentException("@MessageId must be present. Key: " + from + "; Key Type: " + from.getClass());
         }
         String messageId = field.getAnnotation(MessageId.class).value();
         return new StringBuilder().append(subsystem).append(SEPARATOR).append(messageId).append(" ").toString();
      }
      
   }
   
   private static final String SEPARATOR = "-";
   
   private final ConcurrentMap<Enum<?>, String> messagePrefixCache;
   
   public WeldMessageConveyor(Locale locale, String subsystem)
   {
      super(locale);
      this.messagePrefixCache = new MapMaker().makeComputingMap(new ComputeMessagePrefix(subsystem));
   }
   
   @Override
   public <E extends Enum<?>> String getMessage(E key, Object... args) throws MessageConveyorException
   {
      return new StringBuilder().append(getMessagePrefix(key)).append(super.getMessage(key, args)).toString();
   }
   
   private <E extends Enum<?>> String getMessagePrefix(final E key)
   {
      return messagePrefixCache.get(key);
   }

}
