package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Field;

import org.jboss.webbeans.introspector.AnnotatedField;

interface WrappableAnnotatedField<T> extends AnnotatedField<T>, WrappableAnnotatedItem<T, Field>
{
   
}
