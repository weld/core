package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Method;

import org.jboss.webbeans.introspector.AnnotatedMember;
import org.jboss.webbeans.introspector.AnnotatedMethod;

interface WrappableAnnotatedMethod<T> extends AnnotatedMethod<T>, WrappableAnnotatedItem<T, Method>
{
   
}
