package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;

interface WrappableAnnotatedAnnotation<T extends Annotation> extends AnnotatedAnnotation<T>, WrappableAnnotatedType<T>
{
   
}
