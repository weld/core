package org.jboss.webbeans.util;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterable<T> implements Iterable<T>
{
   
   private EnumerationIterator<T> iterator;
   
   public EnumerationIterable(Enumeration<T> enumeration)
   {
      this.iterator = new EnumerationIterator<T>(enumeration);
   }
   
   public Iterator<T> iterator()
   {
      return iterator;
   }
   
}
