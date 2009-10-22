package org.jboss.weld.context;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;

import org.jboss.weld.Container;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;

/**
 * A serializable version of contextual that knows how to restore the
 * original bean if necessary
 * 
 * @author pmuir
 * 
 */
public class SerializableContextualImpl<C extends Contextual<I>, I> extends ForwardingContextual<I> implements SerializableContextual<C, I>
{

   @Override
   protected Contextual<I> delegate()
   {
      return get();
   }

   private static final long serialVersionUID = 9161034819867283482L;

   // A directly serializable contextual
   private C serialiazable;
   
   // A cached, transient version of the contextual
   private transient C cached;
   
   // the id of a non-serializable, passivation capable contextual
   private String id;
   
   public SerializableContextualImpl(C contextual)
   {
      if (contextual instanceof Serializable)
      {
         // the contextual is serializable, so we can just use it
         this.serialiazable = contextual;
      }
      else
      {
         // otherwise, generate an id (may not be portable between container instances)
         this.id = Container.instance().deploymentServices().get(ContextualStore.class).putIfAbsent(contextual);
      }
      // cache the contextual
      this.cached = contextual;
   }
   
   public C get()
   {
      if (cached == null)
      {
         loadContextual();
      }
      return cached;
   }
   
   private void loadContextual()
   {
      if (serialiazable != null)
      {
         this.cached = serialiazable;
      }
      else if (id != null)
      {
         this.cached = Container.instance().deploymentServices().get(ContextualStore.class).<C, I>getContextual(id);
      }
   }
   
   @Override
   public boolean equals(Object obj)
   {
      // if the arriving object is also a SerializableContextual, then unwrap it
      if (obj instanceof SerializableContextualImpl<?, ?>)
      {
         return delegate().equals(((SerializableContextualImpl<?, ?>) obj).get());
      }
      else
      {
         return delegate().equals(obj);
      }
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
}
