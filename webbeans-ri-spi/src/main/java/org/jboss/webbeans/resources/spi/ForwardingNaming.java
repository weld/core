package org.jboss.webbeans.resources.spi;


/**
 * An implementation of {@link Naming} which forwards all its method calls
 * to another {@link Naming}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link Naming} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingNaming implements Naming
{
   
   protected abstract Naming delegate();
   
   public void bind(String key, Object value)
   {
      delegate().bind(key, value);
   }
   
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      return delegate().lookup(name, expectedType);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
}
