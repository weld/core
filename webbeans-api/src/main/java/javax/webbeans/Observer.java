package javax.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface Observer<T>
{

   public Class<T> getEventType();
   public Set<Annotation> getEventBindingTypes();
   
   public void notify(Container container, T event);
   
}
