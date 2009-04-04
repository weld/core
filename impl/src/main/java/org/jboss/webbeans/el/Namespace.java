package org.jboss.webbeans.el;

import java.util.HashMap;
import java.util.Map;

/**
 * A namespace for bean names
 * 
 * @author Gavin King
 *
 */
public class Namespace
{
   private final String qualifiedName;
   private final String name;
   private final Map<String, Namespace> children = new HashMap<String, Namespace>();
   
   public Namespace(String name, String qualifiedName) 
   {
      this.name = name;
      this.qualifiedName = qualifiedName;
   }
   
   public Namespace putIfAbsent(String key)
   {
      Namespace result = children.get(key);
      if (result==null)
      {
         result = new Namespace( key, qualifyName(key) );
         children.put(key, result);
      }
      return result;
   }
   
   public Namespace get(String key)
   {
      return children.get(key);
   }
   
   public boolean contains(String key)
   {
      return children.containsKey(key);
   }

   public String getQualifiedName()
   {
      return qualifiedName;
   }
   
   public String qualifyName(String suffix)
   {
      return qualifiedName == null ? suffix : qualifiedName + "." + suffix;
   }
   
   @Override
   public int hashCode()
   {
      return name==null ? 0 : name.hashCode();
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof Namespace)
      {
         Namespace that = (Namespace) other;
         return this.getQualifiedName().equals(that.getQualifiedName());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public String toString()
   {
      return "Namespace(" + ( name==null ? "Root" : name ) + ')';
   }

}
