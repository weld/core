package org.jboss.webbeans.util;

import java.util.Comparator;
import java.util.List;

public class ListComparator<T> implements Comparator<T>
{
   
   private List<T> list;
   
   public ListComparator(List<T> list)
   {
      this.list = list;
   }

   public int compare(T o1, T o2)
   {
      int p1 = list.indexOf(o1);
      int p2 = list.indexOf(o2);
      return p1 - p2;
   }

}
