/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Qualifier;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.ContextualInstance;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.experimental.ExperimentalEventMetadata;
import org.jboss.weld.experimental.Priority;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.MethodInvocationStrategy;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.attributes.SpecialParameterInjectionPoint;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

/**
 * <p>
 * Reference implementation for the ObserverMethod interface, which represents an observer method. Each observer method has an event type which is the class of
 * the event object being observed, and event binding types that are annotations applied to the event parameter to narrow the event notifications delivered.
 * </p>
 *
 * @author David Allen
 * @author Jozef Hartinger
 * @author Marko Luksa
 */
public class ObserverMethodImpl<T, X> implements ObserverMethod<T> {

    public static final String ID_PREFIX = ObserverMethodImpl.class.getPackage().getName();

    public static final String ID_SEPARATOR = "-";

    @SuppressWarnings("serial")
    private static final Type EVENT_METADATA_INSTANCE_TYPE = new TypeLiteral<Instance<EventMetadata>>() {
    }.getType();

    @SuppressWarnings("checkstyle:magicnumber")
    private static final int DEFAULT_PRIORITY = javax.interceptor.Interceptor.Priority.APPLICATION + 500;

    private final Set<Annotation> bindings;
    private final Type eventType;
    protected final BeanManagerImpl beanManager;
    private final Reception reception;
    protected final RIBean<X> declaringBean;
    protected final MethodInjectionPoint<T, ? super X> observerMethod;
    protected TransactionPhase transactionPhase;
    private final String id;

    private final Set<WeldInjectionPointAttributes<?, ?>> injectionPoints;
    private final Set<WeldInjectionPointAttributes<?, ?>> newInjectionPoints;

    private final int priority;
    // this turned out to be noticeable faster than observerMethod.getAnnotated().isStatic()
    private final boolean isStatic;
    private final boolean eventMetadataRequired;

    private final MethodInvocationStrategy notificationStrategy;

    private final boolean isAsync;

    /**
     * Creates an Observer which describes and encapsulates an observer method (8.5).
     *
     * @param observer The observer
     * @param declaringBean The observer bean
     * @param manager The Bean manager
     */
    protected ObserverMethodImpl(final EnhancedAnnotatedMethod<T, ? super X> observer, final RIBean<X> declaringBean, final BeanManagerImpl manager,
            final boolean isAsync) {
        this.beanManager = manager;
        this.declaringBean = declaringBean;
        this.observerMethod = initMethodInjectionPoint(observer, declaringBean, manager);
        EnhancedAnnotatedParameter<?, ? super X> eventParameter = observer.getEnhancedParameters(Observes.class).get(0);
        this.eventType = new HierarchyDiscovery(declaringBean.getBeanClass()).resolveType(eventParameter.getBaseType());
        this.id = createId(observer, declaringBean);

        final Class<? extends Annotation> annotationClass;
        if (isAsync) {
            annotationClass = ObservesAsync.class;
            this.reception = observer.getEnhancedParameters(ObservesAsync.class).get(0).getAnnotation(ObservesAsync.class).notifyObserver();
            // Asynchronous observers may not be transactional
            this.transactionPhase = TransactionPhase.IN_PROGRESS;
        } else {
            annotationClass = Observes.class;
            this.reception = observer.getEnhancedParameters(Observes.class).get(0).getAnnotation(Observes.class).notifyObserver();
            this.transactionPhase = ObserverFactory.getTransactionalPhase(observer);
        }
        this.bindings = manager.getServices().get(SharedObjectCache.class)
                .getSharedSet(observer.getEnhancedParameters(annotationClass).get(0).getMetaAnnotations(Qualifier.class));

        ImmutableSet.Builder<WeldInjectionPointAttributes<?, ?>> injectionPoints = ImmutableSet.builder();
        ImmutableSet.Builder<WeldInjectionPointAttributes<?, ?>> newInjectionPoints = ImmutableSet.builder();
        for (ParameterInjectionPoint<?, ?> injectionPoint : observerMethod.getParameterInjectionPoints()) {
            if (injectionPoint instanceof SpecialParameterInjectionPoint) {
                continue;
            }
            if (injectionPoint.getQualifier(New.class) != null) {
                newInjectionPoints.add(injectionPoint);
            }
            injectionPoints.add(injectionPoint);
        }
        this.injectionPoints = injectionPoints.build();
        this.newInjectionPoints = newInjectionPoints.build();
        Priority priority = eventParameter.getAnnotation(Priority.class);
        if (priority == null) {
            this.priority = DEFAULT_PRIORITY;
        } else {
            this.priority = priority.value();
        }
        this.isStatic = observer.isStatic();
        this.eventMetadataRequired = initMetadataRequired(this.injectionPoints);
        this.notificationStrategy = MethodInvocationStrategy.forObserver(observerMethod, beanManager);
        this.isAsync = isAsync;
    }

    private static boolean initMetadataRequired(Set<WeldInjectionPointAttributes<?, ?>> injectionPoints) {
        for (WeldInjectionPointAttributes<?, ?> ip : injectionPoints) {
            Type type = ip.getType();
            if (EventMetadata.class.equals(type) || ExperimentalEventMetadata.class.equals(type) || EVENT_METADATA_INSTANCE_TYPE.equals(type)) {
                return true;
            }
        }
        return false;
    }

    protected static String createId(final EnhancedAnnotatedMethod<?, ?> observer, final RIBean<?> declaringBean) {
        String typeId = null;
        if (declaringBean instanceof AbstractClassBean<?>) {
            AbstractClassBean<?> classBean = (AbstractClassBean<?>) declaringBean;
            typeId = classBean.getAnnotated().getIdentifier().asString();
        } else {
            typeId = declaringBean.getBeanClass().getName();
        }
        return new StringBuilder().append(ID_PREFIX).append(ID_SEPARATOR).append(ObserverMethod.class.getSimpleName()).append(ID_SEPARATOR).append(typeId)
                .append(".").append(observer.getSignature()).toString();
    }

    protected MethodInjectionPoint<T, ? super X> initMethodInjectionPoint(EnhancedAnnotatedMethod<T, ? super X> observer, RIBean<X> declaringBean,
            BeanManagerImpl manager) {
        return InjectionPointFactory.instance().createMethodInjectionPoint(observer, declaringBean, declaringBean.getBeanClass(), Observes.class, manager);
    }

    public Set<WeldInjectionPointAttributes<?, ?>> getInjectionPoints() {
        return injectionPoints;
    }

    public Set<WeldInjectionPointAttributes<?, ?>> getNewInjectionPoints() {
        return newInjectionPoints;
    }

    /**
     * Performs validation of the observer method for compliance with the specifications.
     */
    private <Y> void checkObserverMethod(EnhancedAnnotatedMethod<T, Y> annotated) {
        // Make sure exactly one and only one parameter is annotated with Observes
        List<EnhancedAnnotatedParameter<?, Y>> eventObjects = annotated.getEnhancedParameters(Observes.class);
        if (this.reception.equals(Reception.IF_EXISTS) && declaringBean.getScope().equals(Dependent.class)) {
            throw EventLogger.LOG.invalidScopedConditionalObserver(this);
        }
        if (eventObjects.size() > 1) {
            throw EventLogger.LOG.multipleEventParameters(this);
        }
        EnhancedAnnotatedParameter<?, Y> eventParameter = eventObjects.iterator().next();
        checkRequiredTypeAnnotations(eventParameter);
        // Check for parameters annotated with @Disposes
        List<?> disposeParams = annotated.getEnhancedParameters(Disposes.class);
        if (disposeParams.size() > 0) {
            throw EventLogger.LOG.invalidDisposesParameter(this);
        }
        // Check annotations on the method to make sure this is not a producer
        // method, initializer method, or destructor method.
        if (this.observerMethod.getAnnotated().isAnnotationPresent(Produces.class)) {
            throw EventLogger.LOG.invalidProducer(this);
        }
        if (this.observerMethod.getAnnotated().isAnnotationPresent(Inject.class)) {
            throw EventLogger.LOG.invalidInitializer(this);
        }
        boolean containerLifecycleObserverMethod = Observers.isContainerLifecycleObserverMethod(this);
        for (EnhancedAnnotatedParameter<?, ?> parameter : annotated.getEnhancedParameters()) {
            // if this is an observer method for container lifecycle event, it must not inject anything besides BeanManager
            if (containerLifecycleObserverMethod && !parameter.isAnnotationPresent(Observes.class) && !BeanManager.class.equals(parameter.getBaseType())) {
                throw EventLogger.LOG.invalidInjectionPoint(this);
            }
        }

    }

    protected void checkRequiredTypeAnnotations(EnhancedAnnotatedParameter<?, ?> eventParameter) {
        if (eventParameter.isAnnotationPresent(WithAnnotations.class)) {
            throw EventLogger.LOG.invalidWithAnnotations(this);
        }
    }

    @Override
    public Class<X> getBeanClass() {
        return declaringBean.getType();
    }

    public RIBean<X> getDeclaringBean() {
        return declaringBean;
    }

    @Override
    public Reception getReception() {
        return reception;
    }

    @Override
    public Set<Annotation> getObservedQualifiers() {
        return bindings;
    }

    @Override
    public Type getObservedType() {
        return eventType;
    }

    @Override
    public TransactionPhase getTransactionPhase() {
        return transactionPhase;
    }

    /**
     * @return the observerMethod
     */
    public MethodInjectionPoint<T, ? super X> getMethod() {
        return observerMethod;
    }

    /**
     * Completes initialization of the observer and allows derived types to override behavior.
     */
    public void initialize(EnhancedAnnotatedMethod<T, ? super X> annotated) {
        checkObserverMethod(annotated);
    }

    @Override
    public void notify(final T event) {
        sendEvent(event);
    }

    /**
     * Invokes the observer method immediately passing the event.
     *
     * @param event The event to notify observer with
     */
    protected void sendEvent(final T event) {
        if (isStatic) {
            sendEvent(event, null, null);
        } else {
            CreationalContext<X> creationalContext = null;
            try {
                Object receiver = getReceiverIfExists(null);
                if (receiver == null && reception != Reception.IF_EXISTS) {
                    // creational context is created only if we need it for obtaining receiver
                    // ObserverInvocationStrategy takes care of creating CC for parameters, if needed
                    creationalContext = beanManager.createCreationalContext(declaringBean);
                    receiver = getReceiverIfExists(creationalContext);
                }
                if (receiver != null) {
                    sendEvent(event, receiver, creationalContext);
                }
            } finally {
                if (creationalContext != null) {
                    creationalContext.release();
                }
            }
        }
    }

    /**
     * Note that {@link CreationalContext#release()} is not invoked within this method.
     *
     * @param event
     * @param receiver
     * @param creationalContext
     */
    protected void sendEvent(T event, Object receiver, CreationalContext<?> creationalContext) {
        try {
            preNotify(event, receiver);
            // As we are working with the contextual instance, we may not have the
            // actual object, but a container proxy (e.g. EJB)
            notificationStrategy.invoke(receiver, observerMethod, event, beanManager, creationalContext);
        } finally {
            postNotify(event, receiver);
        }
    }

    /**
     * Hooks allowing subclasses to perform additional logic just before and just after an event is delivered to an observer method.
     */
    protected void preNotify(T event, Object receiver) {
    }

    protected void postNotify(T event, Object receiver) {
    }

    private Object getReceiverIfExists(CreationalContext<X> creationalContext) {
        try {
            return getReceiver(creationalContext);
        } catch (ContextNotActiveException e) {
            return null;
        }
    }

    protected Object getReceiver(CreationalContext<X> creationalContext) {

        if (creationalContext != null) {
            if (creationalContext instanceof CreationalContextImpl<?>) {
                // Create child creational context so that a dependent observer may be destroyed after the observer method completes
                creationalContext = ((CreationalContextImpl<?>) creationalContext).getCreationalContext(declaringBean);
            }
            return ContextualInstance.get(declaringBean, beanManager, creationalContext);
        }
        // Conditional observer - no creational context is required
        return ContextualInstance.getIfExists(declaringBean, beanManager);
    }

    @Override
    public String toString() {
        return observerMethod.toString();
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ObserverMethodImpl<?, ?> that = (ObserverMethodImpl<?, ?>) obj;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isAsync() {
        return isAsync;
    }

    public boolean isEventMetadataRequired() {
        return eventMetadataRequired;
    }
}
