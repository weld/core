package javax.webbeans;

import java.lang.annotation.Annotation;

public interface Context
{

   public Class<Annotation> getScopeType();
   
   public <T> T get(Container container, ComponentInstance<T> component, boolean create);
   
   public <T> void remove(Container container, ComponentInstance<T> component);
}
