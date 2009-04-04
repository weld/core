package org.jboss.webbeans.el;

import javax.inject.manager.Bean;

public class NamespaceManager
{
   
   private final Namespace root;
   
   public NamespaceManager(Namespace root)
   {
      this.root = root;
   }

   public void register(Bean<?> bean)
   {
      if (bean.getName() != null && bean.getName().indexOf('.') > 0)
      {
         String name = bean.getName().substring(0, bean.getName().lastIndexOf('.'));
         String[] hierarchy = name.split("\\.");
         Namespace namespace = root;
         for (String s : hierarchy)
         {
            namespace = namespace.putIfAbsent(s);
         }
      }
   }
   
   public Namespace getRoot()
   {
      return root;
   }
   
}
