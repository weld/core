package org.jboss.webbeans.introspector.jlr;

import org.jboss.webbeans.introspector.AnnotatedItem;

interface WrappableAnnotatedItem<T, S> extends AnnotatedItem<T, S>
{
   
   AnnotationStore getAnnotationStore();
   
}