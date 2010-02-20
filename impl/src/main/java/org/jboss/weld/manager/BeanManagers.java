package org.jboss.weld.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class BeanManagers
{
   
   private static class BeanManagerTransform implements Transform<BeanManagerImpl>
   {
      
      public static final BeanManagerTransform INSTANCE = new BeanManagerTransform();
      
      public Iterable<BeanManagerImpl> transform(BeanManagerImpl beanManager)
      {
         return beanManager.getAccessibleManagers();
      }
      
   }
   
   private BeanManagers() {}
   
   public static Set<Iterable<BeanManagerImpl>> getAccessibleClosure(BeanManagerImpl beanManager)
   {
      Set<Iterable<BeanManagerImpl>> beanManagers = new HashSet<Iterable<BeanManagerImpl>>();
      beanManagers.add(Collections.singleton(beanManager));
      beanManagers.addAll(buildAccessibleClosure(beanManager, new HashSet<BeanManagerImpl>(), BeanManagerTransform.INSTANCE));
      return beanManagers;
   }
   
   public static <T> Set<Iterable<T>> buildAccessibleClosure(BeanManagerImpl beanManager, Collection<BeanManagerImpl> hierarchy, Transform<T> transform)
   {
      Set<Iterable<T>> result = new HashSet<Iterable<T>>();
      hierarchy.add(beanManager);
      result.add(transform.transform(beanManager));
      for (BeanManagerImpl accessibleBeanManager : beanManager.getAccessibleManagers())
      {
         // Only add if we aren't already in the tree (remove cycles)
         if (!hierarchy.contains(accessibleBeanManager))
         {
            result.addAll(buildAccessibleClosure(accessibleBeanManager, new ArrayList<BeanManagerImpl>(hierarchy), transform));
         }
      }
      return result;
   }

}
