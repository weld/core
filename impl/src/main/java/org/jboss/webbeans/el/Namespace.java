package org.jboss.webbeans.el;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
   private final Map<String, Namespace> children;
   
   /**
    * Create a new namespace hierarchy, creating copies of all children as
    * children of this node
    *
    * @param namespace
    */
   public Namespace(Namespace namespace)
   {
      this(namespace.getName(), namespace.getQualifiedName());
      for (Entry<String, Namespace> entry : namespace.getChildren().entrySet())
      {
         children.put(entry.getKey(), new Namespace(entry.getValue()));
      }
   }
   
   /**
    * Create a new, root, namespace
    * 
    */
   public Namespace()
   {
      this(null, null);
   }
   
   protected Namespace(String name, String qualifiedName) 
   {
      this.name = name;
      this.qualifiedName = qualifiedName;
      this.children  = new HashMap<String, Namespace>();
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
   
   protected Map<String, Namespace> getChildren()
   {
      return children;
   }
   
   protected String getName()
   {
      return name;
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
