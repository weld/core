package org.jboss.webbeans.el;

import java.util.HashMap;
import java.util.Map;

/**
 * A namespace for Seam component names. 
 * 
 * @author Gavin King
 *
 */
public class Namespace
{
   
   private String name;
   private Map<String, Namespace> children = new HashMap<String, Namespace>();
   
   public Namespace(String name) 
   {
      this.name = name;
   }
   
   public Namespace getChild(String key)
   {
      Namespace result = children.get(key);
      if (result==null)
      {
         result = new Namespace( getQualifiedName(key) + '.' );
         children.put(name, result);
      }
      return result;
   }

   public String getQualifiedName(String key)
   {
      return name==null ? key : name + key;
   }
   
   @Override
   public int hashCode()
   {
      return name==null ? 0 : name.hashCode();
   }
   
   @Override
   public boolean equals(Object other)
   {
      if ( !(other instanceof Namespace) )
      {
         return false;
      }
      else
      {
         Namespace ns = (Namespace) other;
         return this.name==ns.name || 
               ( this.name!=null && this.name.equals(ns.name) );
      }
   }
   
   @Override
   public String toString()
   {
      return "Namespace(" + ( name==null ? "Root" : name ) + ')';
   }

}
