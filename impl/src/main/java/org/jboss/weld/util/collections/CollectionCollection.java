package org.jboss.weld.util.collections;

import static com.google.common.collect.Iterators.concat;
import static com.google.common.collect.Iterators.transform;
import static java.util.Arrays.asList;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CollectionCollection<E> extends AbstractCollection<E>
{
   
   private final Collection<Collection<E>> collections;

   public CollectionCollection(Collection<Collection<E>> collections)
   {
      this.collections = collections;
   }
   
   public CollectionCollection(Collection<E>... collections)
   {
      this.collections = asList(collections);
   }
   
   public CollectionCollection(Collection<E> collection1, Collection<E> collection2)
   {
      this.collections = new ArrayList<Collection<E>>();
      this.collections.add(collection1);
      this.collections.add(collection2);
   }

   @Override
   public Iterator<E> iterator()
   {
      return concat(transform(collections.iterator(), IterableToIteratorFunction.<E>instance()));
   }

   @Override
   public int size()
   {
      int i = 0;
      for (Collection<E> collection : collections)
      {
         i += collection.size();
      }
      return i;
   }

   

}
