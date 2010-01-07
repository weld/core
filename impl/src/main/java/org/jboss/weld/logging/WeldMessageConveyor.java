package org.jboss.weld.logging;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.jboss.weld.util.collections.ConcurrentCache;
import org.jboss.weld.util.reflection.SecureReflections;

import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;

public class WeldMessageConveyor extends MessageConveyor
{
   
   private static final String SEPARATOR = "-";
   private final String subsystem;
   
   private final ConcurrentCache<Enum<?>, String> messagePrefixCache;
   
   public WeldMessageConveyor(Locale locale, String subsystem)
   {
      super(locale);
      this.subsystem = subsystem;
      this.messagePrefixCache = new ConcurrentCache<Enum<?>, String>();
   }
   
   @Override
   public <E extends Enum<?>> String getMessage(E key, Object... args) throws MessageConveyorException
   {
      return new StringBuilder().append(getMessagePrefix(key)).append(super.getMessage(key, args)).toString();
   }
   
   private <E extends Enum<?>> String getMessagePrefix(final E key)
   {
      return messagePrefixCache.putIfAbsent(key, new Callable<String>()
      {
         
         public String call() throws Exception
         {
            Field field = SecureReflections.getField(key.getClass(), key.name());
            if (!field.isAnnotationPresent(MessageId.class))
            {
               throw new IllegalArgumentException("@MessageId must be present. Key: " + key + "; Key Type: " + key.getClass());
            }
            String messageId = field.getAnnotation(MessageId.class).value();
            return new StringBuilder().append(subsystem).append(SEPARATOR).append(messageId).append(" ").toString();
         }
         
      });
   }

}
