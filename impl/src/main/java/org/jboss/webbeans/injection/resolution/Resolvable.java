package org.jboss.webbeans.injection.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Something that is resovable by the resolver
 * 
 * @author pmuir
 *
 */
public interface Resolvable
{
   
   public Set<Annotation> getBindings();
   
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
   
   public boolean isAssignableTo(Class<?> clazz);
   
   public Set<Type> getTypes();

}
