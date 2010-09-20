package org.jboss.weld.bootstrap;

import static org.jboss.weld.util.collections.Arrays2.asSet;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.Context;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;

public class ContextHolder<T extends Context>
{

   private final T context;
   private final Class<T> type;
   private final Set<Annotation> qualifiers;
   
   public ContextHolder(T context, Class<T> type, Annotation qualifier)
   {
      super();
      this.context = context;
      this.type = type;
      this.qualifiers = asSet(DefaultLiteral.INSTANCE, AnyLiteral.INSTANCE, qualifier);
   }
   
   public T getContext()
   {
      return context;
   }
   public Class<T> getType()
   {
      return type;
   }
   public Set<Annotation> getQualifiers()
   {
      return qualifiers;
   }
   
}
