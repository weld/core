package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Constructor;

import org.jboss.webbeans.introspector.AnnotatedConstructor;

interface WrappableAnnotatedConstructor<T> extends AnnotatedConstructor<T>, WrappableAnnotatedItem<T, Constructor<T>>
{
   
}
