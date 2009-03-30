package org.jboss.webbeans;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.context.Context;
import javax.context.CreationalContext;
import javax.event.Observer;
import javax.inject.TypeLiteral;
import javax.inject.manager.Bean;
import javax.inject.manager.Decorator;
import javax.inject.manager.InjectionPoint;
import javax.inject.manager.InterceptionType;
import javax.inject.manager.Interceptor;
import javax.inject.manager.Manager;

import org.jboss.webbeans.manager.api.WebBeansManager;

public class ChildManager implements WebBeansManager, Serializable
{
   
   private final WebBeansManager parentManager;
   
   public ChildManager(WebBeansManager manager)
   {
      this.parentManager = manager; 
   }
   
   public void injectNonContextualInstance(Object instance)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public void shutdown()
   {
      throw new UnsupportedOperationException("Must call shutdown() on root manager");
   }
   
   public Manager addBean(Bean<?> bean)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Manager addContext(Context context)
   {
      throw new UnsupportedOperationException("Must add contexts to root manager");
   }
   
   public Manager addDecorator(Decorator decorator)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Manager addInterceptor(Interceptor interceptor)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Manager createActivity()
   {
      return new ChildManager(this);
   }
   
   public void fireEvent(Object event, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Context getContext(Class<? extends Annotation> scopeType)
   {
      return parentManager.getContext(scopeType);
   }
   
   public <T> T getInstance(Bean<T> bean)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Object getInstanceByName(String name)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> T getInstanceByType(Class<T> type, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> T getInstanceToInject(InjectionPoint injectionPoint, CreationalContext<?> creationalContext)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> T getInstanceToInject(InjectionPoint injectionPoint)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Manager parse(InputStream xmlStream)
   {
      throw new UnsupportedOperationException("Can only add XML metadata to root manager");
   }
   
   public <T> Manager removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Set<Bean<?>> resolveByName(String name)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> type, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public List<Decorator> resolveDecorators(Set<Type> types, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
   
   public Manager setCurrent(Class<? extends Annotation> scopeType)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
