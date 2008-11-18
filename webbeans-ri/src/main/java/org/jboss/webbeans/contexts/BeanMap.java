package org.jboss.webbeans.contexts;

import javax.webbeans.manager.Bean;

/**
 * Interface for different implementations of Bean to Bean instance 
 * storage. Used primarily by the contexts.
 * 
 * @author Nicklas Karlsson
 *
 */

public interface BeanMap
{
   /**
    * Gets an instance of a bean from the storage. 
    * 
    * @param bean The bean whose instance to return
    * 
    * @return The instance. Null if not found
    */
   public abstract <T extends Object> T get(Bean<? extends T> bean);
   
   /**
    * Removes an instance of a bean from the storage
    *
    * @param bean The bean whose instance to remove
    * 
    * @return The removed instance. Null if not found in storage.
    */
   public abstract <T extends Object> T remove(Bean<? extends T> bean);

   /**
    * Clears the storage of any bean instances
    */
   public abstract void clear();
   
   /**
    * Returns an Iterable over the current keys in the storage
    * 
    * @return An Iterable over the keys in the storage
    */
   public abstract Iterable<Bean<? extends Object>> keySet();
   
   /**
    * Adds a bean instance to the storage
    * 
    * @param bean The bean type. Used as key
    * 
    * @param instance The instance to add
    * 
    * @return The instance added
    */
   public abstract <T extends Object> T put(Bean<? extends T> bean, T instance);
}