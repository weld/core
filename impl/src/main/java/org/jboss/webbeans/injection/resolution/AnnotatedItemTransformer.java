package org.jboss.webbeans.injection.resolution;

import org.jboss.webbeans.introspector.AnnotatedItem;


public interface AnnotatedItemTransformer
{
   
   public <T, S> AnnotatedItem<T, S> transform(AnnotatedItem<T, S> element);
   
}
