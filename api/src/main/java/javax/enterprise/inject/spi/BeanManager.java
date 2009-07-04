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

package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.el.ELResolver;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ScopeType;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observer;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;

/**
 * The contract between the application and the manager. Also the contract
 * between the manager and Bean, Context and Observer objects.
 * 
 * The application should not normally need to call this interface.
 * 
 * @author Gavin King
 * @author Pete Muir
 * @author Clint Popetz
 * 
 */
public interface BeanManager
{

   /**
    * Obtains a contextual reference for a given bean and a given bean type.
    * 
    * @param bean the Bean object representing the bean
    * @param beanType a bean type that must be implemented by any proxy that is
    *           returned
    * @return a contextual reference representing the bean
    * @throws IllegalArgumentException if the given type is not a bean type of
    *            the given bean
    */
   public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx);

   /**
    * Obtains an instance of bean for a given injection point.
    * 
    * This method should not be called by an application.
    * 
    * @param ij the injection point the instance is needed for
    * @param ctx the context in which the injection is occurring
    * @return an instance of the bean
    * @throws UnsatisfiedResolutionException if no bean can be resolved for the
    *            given type and bindings
    * @throws AmbiguousResolutionException if more than one bean is resolved for
    *            the given type and bindings
    */
   public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx);

   /**
    * Obtain an instance of a {@link CreationalContext} for the given contextual
    * 
    * @param contextual the contextual to create a creational context for
    * @return the {@link CreationalContext} instance
    */
   public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual);

   /**
    * Returns the set of beans which match the given required type and bindings
    * and are accessible to the class into which the BeanManager was injected,
    * according to the rules of typesafe resolution.
    * 
    * Typesafe resolution usually occurs at container deployment time.
    * 
    * @param beanType the type of the beans to be resolved
    * @param bindings the bindings used to restrict the matched beans. If no
    *           bindings are passed to getBeans(), the default binding @Current
    *           is assumed.
    * @return the matched beans
    * @throws IllegalArgumentException if the given type represents a type
    *            variable
    * @throws IllegalArgumentException if two instances of the same binding type
    *            are
    * @throws IllegalArgumentException if an instance of an annotation that is
    *            not a binding type is given
    */
   public Set<Bean<?>> getBeans(Type beanType, Annotation... bindings);

   /**
    * Returns the set of beans which match the given EL name and are accessible
    * to the class into which the BeanManager was injected, according to the
    * rules of EL name resolution.
    * 
    * @param name the name used to restrict the beans matched
    * @return the matched beans
    */
   public Set<Bean<?>> getBeans(String name);

   /**
    * Returns the Bean object representing the most specialized enabled bean
    * registered with the container that specializes the given bean,
    * 
    * @param <X> The type of the bean
    * @param bean The Bean representation of the bean.
    * @return the most specialized enabled bean
    */
   public <X> Bean<? extends X> getMostSpecializedBean(Bean<X> bean);

   /**
    * Returns the PassivationCapableBean with the given identifier.
    */
   public Bean<?> getPassivationCapableBean(String id);

   /**
    * Apply the ambiguous dependency resolution rules
    * 
    * @param <X> The type of the bean
    * @param beans A set of beans of the given type
    * @throws AmbiguousResolutionException if the ambiguous dependency
    *            resolution rules fail
    */
   public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans);

   /**
    * Validates the dependency
    * 
    * @param injectionPoint the injection point to validate
    * @throws an InjectionException if there is a deployment problem (for
    *            example, an unsatisfied or unresolvable ambiguous dependency)
    *            associated with the injection point.
    */
   public void validate(InjectionPoint injectionPoint);

   /**
    * Fire an event
    * 
    * @param event the event object
    * @param bindings the event bindings used to restrict the observers matched
    * @throws IllegalArgumentException if the runtime type of the event object
    *            contains a type variable
    * @throws IllegalArgumentException if two instances of the same binding type
    *            are given
    * @throws IllegalArgumentException if an instance of an annotation that is
    *            not a binding type is given,
    */
   public void fireEvent(Object event, Annotation... bindings);

   /**
    * Obtains observers for an event by considering event type and bindings.
    * 
    * @param <T> the type of the event to obtain
    * @param event the event object
    * @param bindings the bindings used to restrict the matched observers
    * @return the resolved observers
    * @throws IllegalArgumentException if a parameterized type with a type
    *            parameter or a wildcard is passed
    * @throws IllegalArgumentException if an annotation which is not a event
    *            binding type is passed
    * @throws IllegalArgumentException if two instances of the same binding type
    *            are passed
    */
   public <T> Set<ObserverMethod<?, T>> resolveObserverMethods(T event, Annotation... bindings);

   /**
    * Obtains an ordered list of enabled decorators for a set of bean types and
    * a set of bindings
    * 
    * @param types the set of bean types of the decorated bean
    * @param bindings the bindings declared by the decorated bean
    * @return the resolved decorators
    * @throws IllegalArgumentException if the set of bean types is empty
    * @throws IllegalArgumentException if an annotation which is not a binding
    *            type is passed
    * @throws IllegalArgumentException if two instances of the same binding type
    *            are passed
    */
   public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... bindings);

   /**
    * Obtains an ordered list of enabled interceptors for a set interceptor
    * bindings
    * 
    * @param type the type of the interception
    * @param bindings the bindings used to restrict the matched interceptors
    * @return the resolved interceptors
    * @throws IllegalArgumentException if no interceptor binding type is passed
    * @throws IllegalArgumentException if an annotation which is not a
    *            interceptor binding type is passed
    * @throws IllegalArgumentException if two instances of the same binding type
    *            are passed
    */
   public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings);

   /**
    * Determine if the given annotationType is a scope type
    */
   public boolean isScopeType(Class<? extends Annotation> annotationType);

   /**
    * Determine if the given annotationType is a binding type
    */
   public boolean isBindingType(Class<? extends Annotation> annotationType);

   /**
    * Determine if the given annotationType is an interceptor binding type
    */
   public boolean isInterceptorBindingType(Class<? extends Annotation> annotationType);

   /**
    * Determine if the given annotationType is a stereotype
    */
   public boolean isStereotype(Class<? extends Annotation> annotationType);

   /**
    * Return a ScopeType definition type for a given annotation representing a
    * scope type
    */
   public ScopeType getScopeDefinition(Class<? extends Annotation> scopeType);

   /**
    * Obtain the set of interceptor binding types meta-annotatinos for the given
    * binding type annotation
    */
   public Set<Annotation> getInterceptorBindingTypeDefinition(Class<? extends Annotation> bindingType);

   /**
    * Obtain the set of binding types meta-annotations for the given stereotype
    * annotation
    */
   public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype);
   
   /**
    * Obtain an active context instance for the given scope type.
    * 
    * @param scopeType the scope to get the context instance for
    * @return the context instance
    * @throws ContextNotActiveException if no active contexts exist for the
    *            given scope type
    * @throws IllegalArgumentException if more than one active context exists
    *            for the given scope type
    */
   public Context getContext(Class<? extends Annotation> scopeType);
   
   /**
    * Returns the ELResolver for integration with the servlet engine and JSF
    * implementation This resolver will return a contextual instance of a bean
    * if the name for resolution resolves to exactly one bean
    */
   public ELResolver getELResolver();
   
   /**
    * Get an {@link AnnotatedType} for the given class
    * @param <T> the type
    * @param type the type
    * @return the {@link AnnotatedType}
    */
   public <T> AnnotatedType<T> createAnnotatedType(Class<T> type);
   
   /**
    * Returns an InjectionTarget to allow injection into custom beans or
    * non-contextual instances by portable extensions.
    * 
    * The container ignores the annotations and types declared by the elements
    * of the actual Java class and uses the metadata provided via the Annotated
    * interface instead.
    * 
    * @param <T> The type of the AnnotatedType to inspect
    * @param type The AnnotatedType to inspect
    * @returns a container provided instance of InjectionTarget for the given
    *          type
    * @throws IllegalArgumentException if there is a definition error associated
    *            with any injection point of the type.
    */
   public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type);
   
   /**
    * Allows a new bean to be registered. This fires a ProcessBean event and
    * then registers a new bean with the container, thereby making it available
    * for injection into other beans.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param bean the bean to register
    */
   @Deprecated
   public void addBean(Bean<?> bean);

   /**
    * Exposes the list of enabled deployment types, in order of lower to higher
    * precedence, This method may be used by portable extensions to discover
    * information about the deployment.
    */
   @Deprecated
   public List<Class<? extends Annotation>> getEnabledDeploymentTypes();

   /**
    * Associate a custom Context with a scope.
    * 
    * This method may be called at any time in the applications lifecycle.
    * 
    * @param context the context to register
    */
   @Deprecated
   public void addContext(Context context);

}
