package javax.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface Container
{

   public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes);

   public <T> T getInstanceByType(TypeLiteral<T> type,
         Annotation... bindingTypes);

   public <T> T resolveByType(Class<T> apiType, Annotation... bindingTypes);

   public <T> T resolveByType(TypeLiteral<T> apiType,
         Annotation... bindingTypes);

   public Object getInstanceByName(String name);

   public Set<ComponentInstance> resolveByName(String name);

   public void fireEvent(Object event, Annotation... bindings);
   
   public void addObserver(Observer observer);
   
   public void removeObserver(Observer observer);
   
   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings);
   
   public void addContext(Context context);
   
   public Context getContext(Class<Annotation> scopeType);
   
   public Container addComponent(ComponentInstance component);

}
