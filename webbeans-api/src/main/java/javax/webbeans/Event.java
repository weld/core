package javax.webbeans;

import java.lang.annotation.Annotation;

public interface Event<T>
{

   public void fire(T event, Annotation... bindings);
   
}
