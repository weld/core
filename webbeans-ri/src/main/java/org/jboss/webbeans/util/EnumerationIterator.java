package org.jboss.webbeans.util;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<T> implements Iterator<T>
{
   private Enumeration e;

   public EnumerationIterator(Enumeration e)
   {
      this.e = e;
   }

   public boolean hasNext()
   {
      return e.hasMoreElements();
   }

   public T next()
   {
      return (T) e.nextElement();
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }
   
}
