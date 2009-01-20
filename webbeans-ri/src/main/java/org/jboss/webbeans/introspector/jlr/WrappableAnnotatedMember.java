package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Member;

import org.jboss.webbeans.introspector.AnnotatedMember;

interface WrappableAnnotatedMember<T, S extends Member> extends AnnotatedMember<T, S>, WrappableAnnotatedItem<T, S>
{
   
}
