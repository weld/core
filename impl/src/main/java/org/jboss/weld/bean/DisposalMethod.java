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
package org.jboss.weld.bean;

import static org.jboss.weld.logging.messages.BeanMessage.INCONSISTENT_ANNOTATIONS_ON_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.METHOD_NOT_BUSINESS_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.MULTIPLE_DISPOSE_PARAMS;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.SecureReflections;

public class DisposalMethod<X, T> extends AbstractReceiverBean<X, T, Method> {

    protected MethodInjectionPoint<T, ? super X> disposalMethodInjectionPoint;
    private WeldParameter<?, ? super X> disposesParameter;

    public static <X, T> DisposalMethod<X, T> of(BeanManagerImpl manager, WeldMethod<T, ? super X> method, AbstractClassBean<X> declaringBean, ServiceRegistry services) {
        return new DisposalMethod<X, T>(manager, method, declaringBean, services);
    }

    protected DisposalMethod(BeanManagerImpl beanManager, WeldMethod<T, ? super X> disposalMethod, AbstractClassBean<X> declaringBean, ServiceRegistry services) {
        super(new StringBuilder().append(DisposalMethod.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getWeldAnnotated().getName()).append(disposalMethod.getSignature().toString()).toString(), declaringBean, beanManager, services);
        this.disposalMethodInjectionPoint = MethodInjectionPoint.of(beanManager.getContextId(), this, disposalMethod);
        initQualifiers();
        initType();
        initTypes();
        initStereotypes();
        addInjectionPoints(Beans.getParameterInjectionPoints(beanManager.getContextId(), this, disposalMethodInjectionPoint));
    }

    private void initDisposesParameter() {
        this.disposesParameter = getWeldAnnotated().getWeldParameters(Disposes.class).get(0);
    }

    public WeldParameter<?, ? super X> getDisposesParameter() {
        return disposesParameter;
    }

    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        super.initialize(environment);
        checkDisposalMethod();
        initDisposesParameter();
    }

    protected void initType() {
        this.type = cast(disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getJavaClass());
    }

    @Override
    public WeldMethod<T, ? super X> getWeldAnnotated() {
        return disposalMethodInjectionPoint;
    }

    @Override
    protected void initQualifiers() {
        // At least 1 parameter exists, already checked in constructor
        this.qualifiers = new HashSet<Annotation>();
        this.qualifiers.addAll(disposalMethodInjectionPoint.getWeldParameters().get(0).getQualifiers());
        initDefaultQualifiers();
    }

    /**
     * Initializes the API types
     */
    @Override
    protected void initTypes() {
        Set<Type> types = new HashSet<Type>();
        types.addAll(disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getTypeClosure());
        types.add(Object.class);
        super.types = types;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return null;
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public boolean isNullable() {
        // Not relevant
        return false;
    }

    @Override
    public boolean isPassivationCapableBean() {
        // Not relevant
        return false;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        // Not relevant
        return false;
    }

    @Override
    public boolean isProxyable() {
        return true;
    }

    public T create(CreationalContext<T> creationalContext) {
        // Not Relevant
        return null;
    }

    public void invokeDisposeMethod(Object instance) {
        CreationalContext<T> creationalContext = beanManager.createCreationalContext(this);
        Object receiverInstance = getReceiver(creationalContext, creationalContext);
        if (receiverInstance == null) {
            disposalMethodInjectionPoint.invokeWithSpecialValue(null, Disposes.class, instance, beanManager, creationalContext, IllegalArgumentException.class);
        } else {
            disposalMethodInjectionPoint.invokeOnInstanceWithSpecialValue(receiverInstance, Disposes.class, instance, beanManager, creationalContext, IllegalArgumentException.class);
        }
        creationalContext.release();
    }

    private void checkDisposalMethod() {
        if (disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).size() > 1) {
            throw new DefinitionException(MULTIPLE_DISPOSE_PARAMS, disposalMethodInjectionPoint);
        }
        if (disposalMethodInjectionPoint.getAnnotatedParameters(Observes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Observes", "@Disposes", disposalMethodInjectionPoint);
        }
        if (disposalMethodInjectionPoint.getAnnotation(Inject.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Intitializer", "@Disposes", disposalMethodInjectionPoint);
        }
        if (disposalMethodInjectionPoint.getAnnotation(Produces.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Disposes", disposalMethodInjectionPoint);
        }
        if (disposalMethodInjectionPoint.getAnnotation(Specializes.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Specialized", "@Disposes", disposalMethodInjectionPoint);
        }
        if (getDeclaringBean() instanceof SessionBean<?>) {
            boolean methodDeclaredOnTypes = false;
            // TODO use annotated item?
            for (Type type : getDeclaringBean().getTypes()) {
                if (type instanceof Class<?>) {
                    Class<?> clazz = (Class<?>) type;
                    if (SecureReflections.isMethodExists(clazz, disposalMethodInjectionPoint.getName(), disposalMethodInjectionPoint.getParameterTypesAsArray())) {
                        methodDeclaredOnTypes = true;
                        continue;
                    }
                }
            }
            if (!methodDeclaredOnTypes) {
                throw new DefinitionException(METHOD_NOT_BUSINESS_METHOD, this, getDeclaringBean());
            }
        }
    }

    @Override
    protected void checkType() {

    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    protected String getDefaultName() {
        return disposalMethodInjectionPoint.getPropertyName();
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // No-op. Producer method dependent objects are destroyed in producer method bean
    }

    @Override
    public AbstractBean<?, ?> getSpecializedBean() {
        // Doesn't support specialization
        return null;
    }

    @Override
    protected void initScope() {
        // Disposal methods aren't scoped
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "Disposer method [" + getDisposesParameter().getDeclaringCallable() + "]";
    }

    @Override
    public boolean hasDefaultProducer() {
        return true; // cannot be replaced by an extension
    }
}
