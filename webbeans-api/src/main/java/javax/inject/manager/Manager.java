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

package javax.inject.manager;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.context.Context;
import javax.context.ContextNotActiveException;
import javax.context.CreationalContext;
import javax.event.Observer;
import javax.inject.AmbiguousDependencyException;
import javax.inject.DuplicateBindingTypeException;
import javax.inject.TypeLiteral;
import javax.inject.UnsatisfiedDependencyException;

/**
 * The contract between the application and the manager. Also the contract
 * between the manager and Bean, Context and Observer objects.
 * 
 * The application should not normally need to call this interface.
 * 
 * @author Gavin King
 * @author Pete Muir
 * 
 */
public interface Manager
{
   
   /**
    * Allows beans to be matched to injection point by considering bean type,
    * bindings, and deployment precedence.
    * 
    * Typesafe resolution usually occurs at container deployment time.
    * 
    * @param <T>
    *           the type of the beans to be resolved
    * @param type
    *           the type of the beans to be resolved
    * @param bindings
    *           the bindings used to restrict the matched beans
    * @return the matched beans
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindings);
   
   /**
    * Allows beans to be matched to injection point by considering bean type,
    * bindings, and deployment precedence.
    * 
    * Typesafe resolution usually occurs at container deployment time.
    * 
    * @param <T>
    *           the type of the beans to be resolved
    * @param type
    *           the type of the beans to be resolved
    * @param bindings
    *           the bindings used to restrict the matched beans
    * @return the matched beans
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> type, Annotation... bindings);
   
   /**
    * Obtains an instance of a bean by considering bean type, bindings, and
    * deployment precedence.
    * 
    * @param <T>
    *           the type of the bean to obtain
    * @param type
    *           the type of the bean to obtain
    * @param bindings
    *           the bindings used to restrict the matched beans
    * @return an instance of the bean
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    * @throws UnsatisfiedDependencyException
    *            if no bean can be resolved for the given type and bindings
    * @throws AmbiguousDependencyException
    *            if more than one bean is resolved for the given type and
    *            bindings
    */
   public <T> T getInstanceByType(Class<T> type, Annotation... bindings);
   
   /**
    * Obtains an instance of a bean by considering bean type, bindings, and
    * deployment precedence.
    * 
    * @param <T>
    *           the type of the bean to obtain
    * @param type
    *           the type of the bean to obtain
    * @param bindings
    *           the bindings used to restrict the matched beans
    * @return an instance of the bean
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    * @throws UnsatisfiedDependencyException
    *            if no bean can be resolved for the given type and bindings
    * @throws AmbiguousDependencyException
    *            if more than one bean is resolved for the given type and
    *            bindings
    */
   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindings);
   
   /**
    * Allows beans to be matched by considering the bean name and deployment
    * precedence.
    * 
    * Used in an environment that doesn't support typing such EL.
    * 
    * @param name
    *           the name used to restrict the beans matched
    * @return the matched beans
    */
   public Set<Bean<?>> resolveByName(String name);
   
   /**
    * Obtains an instance of a bean by considering the bean name and deployment
    * precedence.
    * 
    * Used in an environment that doesn't support typing such EL.
    * 
    * @param name
    *           the name used to restrict the beans matched
    * @return an instance of the bean or null if no beans matched
    * @throws AmbiguousDependencyException
    *            if more than one bean matches
    */
   public Object getInstanceByName(String name);
   
   /**
    * Obtains an instance of a bean
    * 
    * @param <T>
    *           the type of the bean
    * @param bean
    *           the bean to obtain an instance of
    * @return an instance of the bean
    */
   public <T> T getInstance(Bean<T> bean);
   
   /**
    * Obtains an instance of bean for a given injection point.
    * 
    * This method should not be called by an application.
    * 
    * @param <T>
    *           the type of the bean
    * @param injectionPoint
    *           the injection point the instance is needed for
    * @param creationalContext
    *           the context in which the injection is occurring
    * @return an instance of the bean
    * @throws UnsatisfiedDependencyException
    *            if no bean can be resolved for the given type and bindings
    * @throws AmbiguousDependencyException
    *            if more than one bean is resolved for the given type and
    *            bindings
    */
   public <T> T getInstanceToInject(InjectionPoint injectionPoint, CreationalContext<?> creationalContext);
   
   /**
    * Obtains an instance of bean for a given injection point.
    * 
    * This method should not be called by an application.
    * 
    * @param <T>
    *           the type of the bean
    * @param injectionPoint
    *           the injection point the instance is needed for
    * @return an instance of the bean
    * @throws UnsatisfiedDependencyException
    *            if no bean can be resolved for the given type and bindings
    * @throws AmbiguousDependencyException
    *            if more than one bean is resolved for the given type and
    *            bindings
    */
   public <T> T getInstanceToInject(InjectionPoint injectionPoint);
   
   /**
    * Fire an event
    * 
    * @param event
    *           the event object
    * @param bindings
    *           the event bindings used to restrict the observers matched
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    */
   public void fireEvent(Object event, Annotation... bindings);
   
   /**
    * Obtain an active context instance for the given scope type.
    * 
    * @param scopeType
    *           the scope to get the context instance for
    * @return the context instance
    * @throws ContextNotActiveException
    *            if no active contexts exist for the given scope type
    * @throws IllegalArgumentException
    *            if more than one active context exists for the given scope type
    */
   public Context getContext(Class<? extends Annotation> scopeType);
   
   /**
    * Associate a custom Context with a scope.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param context
    *           the context to register
    * @return the manager the context was registered with
    */
   public Manager addContext(Context context);
   
   /**
    * Allows a new bean to be registered.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param bean
    *           the bean to register
    * @return the manager the bean was registered with
    */
   public Manager addBean(Bean<?> bean);
   
   /**
    * Allows a new interceptor to be registered.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param interceptor
    *           the interceptor to register
    * @return the manager the interceptor was registered with
    */
   public Manager addInterceptor(Interceptor interceptor);
   
   /**
    * Allows a new decorator to be registered.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param decorator
    *           the decorator to register
    * @return the manager the decorator was registered with
    */
   public Manager addDecorator(Decorator decorator);
   
   /**
    * Allows additional XML based to be provided.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param xmlStream
    *           the XML metadata
    * @return the manager the XML metadata was registered with
    */
   public Manager parse(InputStream xmlStream);
   
   /**
    * Create a new child activity. A child activity inherits all beans,
    * interceptors, decorators, observers, and contexts defined by its direct
    * and indirect parent activities.
    * 
    * This method should not be called by the application.
    * 
    * @return the child activity
    */
   public Manager createActivity();
   
   /**
    * Associate an activity with the current context for a normal scope
    * 
    * @param scopeType
    *           the scope to associate the activity with
    * @return the activity
    * @throws ContextNotActiveException
    *            if the given scope is inactive
    * @throws IllegalArgumentException
    *            if the given scope is not a normal scope
    */
   public Manager setCurrent(Class<? extends Annotation> scopeType);
   
   /**
    * Register an observer with the container
    * 
    * @param <T>
    *           the type of the observer
    * @param observer
    *           the observer to register
    * @param eventType
    *           the event type the observer observes
    * @param bindings
    *           event bindings to further restrict the events observed
    * @return the manager the observer was registered with
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings);
   
   /**
    * Register an observer with the container
    * 
    * @param <T>
    *           the type of the observer
    * @param observer
    *           the observer to register
    * @param eventType
    *           the event type the observer observes
    * @param bindings
    *           event bindings to further restrict the events observed
    * @return the manager the observer was registered with
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a event binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings);
   
   /**
    * Remove an observer registration
    * 
    * @param <T>
    *           the type of the observer
    * @param observer
    *           the observer to register
    * @param eventType
    *           the event type the observer obseres
    * @param bindings
    *           event bindings to further restrict the events observed
    * @return the manager the observer was registered with
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a event binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Manager removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings);
   
   /**
    * Remove an observer registration
    * 
    * @param <T>
    *           the type of the observer
    * @param observer
    *           the observer to register
    * @param eventType
    *           the event type the observer obseres
    * @param bindings
    *           event bindings to further restrict the events observed
    * @return the manager the observer was registered with
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a event binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings);
   
   /**
    * Obtains observers for an event by considering event type and bindings.
    * 
    * @param <T>
    *           the type of the event to obtain
    * @param event
    *           the event object
    * @param bindings
    *           the bindings used to restrict the matched observers
    * @return the resolved observers
    * @throws IllegalArgumentException
    *            if a parameterized type with a type parameter or a wildcard is
    *            passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a event binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings);
   
   /**
    * Obtains an ordered list of enabled interceptors for a set interceptor
    * bindings
    * 
    * @param type
    *           the type of the interception
    * @param bindings
    *           the bindings used to restrict the matched interceptors
    * @return the resolved interceptors
    * @throws IllegalArgumentException
    *            if no interceptor binding type is passed
    * @throws IllegalArgumentException
    *            if an annotation which is not a interceptor binding type is
    *            passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings);
   
   /**
    * Obtains an ordered list of enabled decorators for a set of bean types and
    * a set of bindings
    * 
    * @param types
    *           the set of bean types of the decorated bean
    * @param bindings
    *           the bindings declared by the decorated bean
    * @return the resolved decorators
    * @throws IllegalArgumentException
    *            if the set of bean types is empty
    * @throws IllegalArgumentException
    *            if an annotation which is not a binding type is passed
    * @throws DuplicateBindingTypeException
    *            if two instances of the same binding type are passed
    */
   public List<Decorator> resolveDecorators(Set<Type> types, Annotation... bindings);
   
}
