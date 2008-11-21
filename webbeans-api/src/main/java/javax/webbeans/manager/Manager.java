/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.webbeans.manager;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.webbeans.Observer;
import javax.webbeans.TypeLiteral;

/**
 * The contract between the application and the Web Bean manager. Also the
 * contract between the Web Bean manager and Bean, Context and Observer objects.
 * 
 * @author Gavin King
 * 
 */
public interface Manager
{

   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindings);

   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> apiType,
         Annotation... bindings);

   public <T> T getInstanceByType(Class<T> type, Annotation... bindings);

   public <T> T getInstanceByType(TypeLiteral<T> type,
         Annotation... bindings);

   public Set<Bean<?>> resolveByName(String name);

   public Object getInstanceByName(String name);

   public <T> T getInstance(Bean<T> bean);

   public void fireEvent(Object event, Annotation... bindings);

   public Context getContext(Class<? extends Annotation> scopeType);

   public Manager addContext(Context context);

   public Manager addBean(Bean<?> bean);

   public Manager addInterceptor(Interceptor interceptor);

   public Manager addDecorator(Decorator decorator);

   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType,
         Annotation... bindings);

   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType,
         Annotation... bindings);

   public <T> Manager removeObserver(Observer<T> observer, Class<T> eventType,
         Annotation... bindings);

   public <T> Manager removeObserver(Observer<T> observer,
         TypeLiteral<T> eventType, Annotation... bindings);

   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings);

   public List<Interceptor> resolveInterceptors(InterceptionType type,
         Annotation... interceptorBindings);

   public List<Decorator> resolveDecorators(Set<Class<?>> types,
         Annotation... bindings);

}
