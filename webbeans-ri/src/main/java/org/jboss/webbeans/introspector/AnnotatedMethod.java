package org.jboss.webbeans.introspector;

import java.lang.reflect.Method;

/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedMethod extends AnnotatedItem
{
   
   public Method getAnnotatedMethod();

}
