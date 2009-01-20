package org.jboss.webbeans.introspector.jlr;

import org.jboss.webbeans.introspector.AnnotatedType;

interface WrappableAnnotatedType<T> extends AnnotatedType<T>, WrappableAnnotatedItem<T, Class<T>>
{
   
}