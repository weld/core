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
package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.logging.ResolutionLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.reflection.Reflections;

public class ResolvableBuilder {

    private static final Class<?>[] FACADE_TYPES = new Class<?>[] { Event.class, Instance.class, WeldEvent.class,
        WeldInstance.class, Provider.class, InterceptionFactory.class };
    private static final Class<?>[] METADATA_TYPES = new Class<?>[] { Interceptor.class, Decorator.class, Bean.class };
    private static final Set<QualifierInstance> ANY_SINGLETON = Collections.singleton(QualifierInstance.ANY);

    protected Class<?> rawType;
    protected final Set<Type> types;
    protected final Set<QualifierInstance> qualifierInstances;
    protected Bean<?> declaringBean;
    private final MetaAnnotationStore store;
    protected boolean delegate;

    public ResolvableBuilder(final MetaAnnotationStore store) {
        this.store = store;
        this.types = new HashSet<Type>();
        this.qualifierInstances = new HashSet<QualifierInstance>();
    }

    public ResolvableBuilder(BeanManagerImpl manager) {
        this(manager.getServices().get(MetaAnnotationStore.class));
    }

    public ResolvableBuilder(Type type, final BeanManagerImpl beanManager) {
        this(beanManager);
        if (type != null) {
            this.rawType = Reflections.getRawType(type);
            if (rawType == null || type instanceof TypeVariable<?>) {
                throw ResolutionLogger.LOG.cannotExtractRawType(type);
            }
            this.types.add(type);
        }
    }

    public ResolvableBuilder(InjectionPoint injectionPoint, final BeanManagerImpl manager) {
        this(injectionPoint.getType(), manager);
        addQualifiers(injectionPoint.getQualifiers(), injectionPoint);
        setDeclaringBean(injectionPoint.getBean());
        this.delegate = injectionPoint.isDelegate();
    }

    public ResolvableBuilder setDeclaringBean(Bean<?> declaringBean) {
        this.declaringBean = declaringBean;
        return this;
    }

    public ResolvableBuilder addType(Type type) {
        this.types.add(type);
        return this;
    }

    public ResolvableBuilder addTypes(Set<Type> types) {
        this.types.addAll(types);
        return this;
    }

    public boolean isDelegate() {
        return delegate;
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    public Resolvable create() {
        if (qualifierInstances.isEmpty()) {
            this.qualifierInstances.add(QualifierInstance.DEFAULT);
        }
        for (Type type : types) {
            Class<?> rawType = Reflections.getRawType(type);
            for (Class<?> facadeType : FACADE_TYPES) {
                if (facadeType.equals(rawType)) {
                    return createFacade(facadeType);
                }
            }
            for (Class<?> metadataType : METADATA_TYPES) {
                if (metadataType.equals(rawType)) {
                    return createMetadataProvider(metadataType);
                }
            }
        }
        return new ResolvableImpl(rawType, types, declaringBean, qualifierInstances, delegate);
    }

    private Resolvable createFacade(Class<?> rawType) {
        Set<Type> types = Collections.<Type>singleton(rawType);
        return new ResolvableImpl(rawType, types, declaringBean, ANY_SINGLETON, delegate);
    }

    // just as facade but we keep the qualifiers so that we can recognize Bean from @Intercepted Bean.
    private Resolvable createMetadataProvider(Class<?> rawType) {
        Set<Type> types = Collections.<Type>singleton(rawType);
        return new ResolvableImpl(rawType, types, declaringBean, qualifierInstances, delegate);
    }

    public ResolvableBuilder addQualifier(Annotation qualifier) {
        return addQualifier(qualifier, null);
    }

    private ResolvableBuilder addQualifier(Annotation qualifier, InjectionPoint injectionPoint) {
        QualifierInstance qualifierInstance = QualifierInstance.of(qualifier, store);
        final Class<? extends Annotation> annotationType = qualifierInstance.getAnnotationClass();
        if (injectionPoint != null && annotationType.equals(Named.class)) {
            Named named = (Named) qualifier;
            if (named.value().equals("")) {
                // WELD-1739
                // This is an injection point with an @Named qualifier, with no value specified, we need to assume the name of the field or parameter is the
                // value
                Member member = injectionPoint.getMember();
                if (member instanceof Executable) {
                    // Method or constructor injection
                    Executable executable = (Executable) member;
                    AnnotatedParameter<?> annotatedParameter = (AnnotatedParameter<?>) injectionPoint.getAnnotated();
                    Parameter parameter = executable.getParameters()[annotatedParameter.getPosition()];
                    named = new NamedLiteral(parameter.getName());
                } else {
                    named = new NamedLiteral(injectionPoint.getMember().getName());
                }

                qualifier = named;
                qualifierInstance = QualifierInstance.of(named, store);

            }
        }

        checkQualifier(qualifier, qualifierInstance, annotationType);
        this.qualifierInstances.add(qualifierInstance);
        return this;
    }

    /**
     * Adds a given qualifier without any checks. This method should be used with care.
     */
    public ResolvableBuilder addQualifierUnchecked(QualifierInstance qualifier) {
        this.qualifierInstances.add(qualifier);
        return this;
    }

    public ResolvableBuilder addQualifiers(Annotation[] qualifiers) {
        for (Annotation qualifier : qualifiers) {
            addQualifier(qualifier);
        }
        return this;
    }

    public ResolvableBuilder addQualifiers(Collection<Annotation> qualifiers) {
        return addQualifiers(qualifiers, null);
    }

    private ResolvableBuilder addQualifiers(Collection<Annotation> qualifiers, InjectionPoint injectionPoint) {
        for (Annotation qualifier : qualifiers) {
            addQualifier(qualifier, injectionPoint);
        }
        return this;
    }

    protected void checkQualifier(Annotation qualifier, final QualifierInstance qualifierInstance, Class<? extends Annotation> annotationType) {
        if (!store.getBindingTypeModel(annotationType).isValid()) {
            throw BeanManagerLogger.LOG.invalidQualifier(qualifierInstance);
        }
        if (!annotationType.isAnnotationPresent(Repeatable.class)) {
            for (QualifierInstance checkedQualifier : qualifierInstances) {
                if (annotationType.equals(checkedQualifier.getAnnotationClass())) {
                    throw BeanManagerLogger.LOG.duplicateQualifiers(qualifierInstances);
                }
            }
        }
    }

    protected static class ResolvableImpl implements Resolvable {

        private final Set<QualifierInstance> qualifierInstances;
        private final Set<Type> typeClosure;
        private final Class<?> rawType;
        private final Bean<?> declaringBean;
        private final boolean delegate;
        private final int hashCode;

        protected ResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Bean<?> declaringBean, final Set<QualifierInstance> qualifierInstances, boolean delegate) {
            this.typeClosure = typeClosure;
            this.rawType = rawType;
            this.declaringBean = declaringBean;
            this.qualifierInstances = qualifierInstances;
            this.delegate = delegate;
            this.hashCode = calculateHashCode();
        }

        @SuppressWarnings("checkstyle:magicnumber")
        private int calculateHashCode() {
            int hashCode = 17;
            hashCode = 31 * hashCode + this.getTypes().hashCode();
            return 31 * hashCode + this.qualifierInstances.hashCode();
        }

        @Override
        public Set<QualifierInstance> getQualifiers() {
            return qualifierInstances;
        }

        @Override
        public Set<Type> getTypes() {
            return typeClosure;
        }

        @Override
        public Class<?> getJavaClass() {
            return rawType;
        }

        @Override
        public Bean<?> getDeclaringBean() {
            return declaringBean;
        }

        @Override
        public String toString() {
            return "Types: " + getTypes() + "; Bindings: " + getQualifiers();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ResolvableImpl) {
                ResolvableImpl r = (ResolvableImpl) o;
                return this.getTypes().equals(r.getTypes()) && this.qualifierInstances.equals(r.qualifierInstances);
            }
            return false;
        }

        @Override
        public boolean isDelegate() {
            return delegate;
        }
    }

    protected MetaAnnotationStore getMetaAnnotationStore() {
        return store;
    }
}
