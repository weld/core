package org.jboss.webbeans.introspector;

import java.lang.reflect.Member;

public interface AnnotatedMember<T, S extends Member> extends AnnotatedItem<T, S>
{
   
   public S getMember();
   
}
