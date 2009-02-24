package org.jboss.webbeans.tck.integration.jbossas.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An Enumeration -> Iteratble adaptor
 *  
 * @author Pete Muir
 * @see org.jboss.webbeans.util.EnumerationIterator
 */
class EnumerationIterable<T> implements Iterable<T>
{
   // The enumeration-iteartor
   private EnumerationIterator<T> iterator;
   
   /**
    * Constructor
    * 
    * @param enumeration The enumeration
    */
   public EnumerationIterable(Enumeration<T> enumeration)
   {
      this.iterator = new EnumerationIterator<T>(enumeration);
   }
   
   /**
    * Gets an iterator
    * 
    * @return The iterator
    */
   public Iterator<T> iterator()
   {
      return iterator;
   }
   
}