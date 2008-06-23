package javax.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Argh, this is not the name in the spec but then we have two classes called
 * Component
 */
public abstract class ComponentInstance<T>
{

   public abstract Set<Class> getTypes();
   public abstract Set<Annotation> getBindingTypes();
   public abstract Annotation getScopeType();
   public abstract Annotation getComponentType();
   public abstract String getName();
   
   public abstract T create(Container container);
   public abstract void destroy(Container container, T instance);

}