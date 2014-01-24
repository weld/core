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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Named;
import javax.inject.Provider;

import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.logging.ResolutionLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.reflection.Reflections;

public class ResolvableBuilder {

    private static final Class<?>[] FACADE_TYPES = new Class<?>[] { Event.class, Instance.class, Provider.class };
    private static final Class<?>[] METADATA_TYPES = new Class<?>[] { Interceptor.class, Decorator.class, Bean.class };
    private static final Set<QualifierInstance> ANY_SINGLETON = Collections.singleton(QualifierInstance.ANY);

    protected Class<?> rawType;
    protected final Set<Type> types;
    protected final Set<Annotation> qualifiers;
    protected final Set<QualifierInstance> qualifierInstances;
    protected final Map<Class<? extends Annotation>, Annotation> mappedQualifiers;
    protected Bean<?> declaringBean;
    private final MetaAnnotationStore store;
    protected boolean delegate;

    public ResolvableBuilder(final MetaAnnotationStore store) {
        this.store = store;
        this.types = new HashSet<Type>();
        this.qualifiers = new HashSet<Annotation>();
        this.mappedQualifiers = new HashMap<Class<? extends Annotation>, Annotation>();
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
        addQualifiers(injectionPoint.getQualifiers());
        if (mappedQualifiers.containsKey(Named.class) && injectionPoint.getMember() instanceof Field) {
            Named named = (Named) mappedQualifiers.get(Named.class);
            QualifierInstance qualifierInstance = QualifierInstance.of(named, store);
            if (named.value().equals("")) {
                qualifiers.remove(named);
                qualifierInstances.remove(qualifierInstance);
                // This is field injection point with an @Named qualifier, with no value specified, we need to assume the name of the field is the value
                named = new NamedLiteral(injectionPoint.getMember().getName());
                qualifierInstance = QualifierInstance.of(named, store);
                qualifiers.add(named);
                qualifierInstances.add(qualifierInstance);
                mappedQualifiers.put(Named.class, named);
            }
        }
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
        if (qualifiers.size() == 0) {
            this.qualifierInstances.add(QualifierInstance.of(DefaultLiteral.INSTANCE, store));
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
        return new ResolvableImpl(rawType, types, mappedQualifiers, declaringBean, qualifierInstances, delegate);
    }

    private Resolvable createFacade(Class<?> rawType) {
        Set<Type> types = Collections.<Type>singleton(rawType);
        return new ResolvableImpl(rawType, types, mappedQualifiers, declaringBean, ANY_SINGLETON, delegate);
    }

    // just as facade but we keep the qualifiers so that we can recognize Bean from @Intercepted Bean.
    private Resolvable createMetadataProvider(Class<?> rawType) {
        Set<Type> types = Collections.<Type>singleton(rawType);
        return new ResolvableImpl(rawType, types, mappedQualifiers, declaringBean, qualifierInstances, delegate);
    }

    public ResolvableBuilder addQualifier(Annotation qualifier) {
        // Handle the @New qualifier special case
        QualifierInstance qualifierInstance = QualifierInstance.of(qualifier, store);
        final Class<? extends Annotation> annotationType = qualifierInstance.getAnnotationClass();
        if (annotationType.equals(New.class)) {
            New newQualifier = New.class.cast(qualifier);
            if (newQualifier.value().equals(New.class) && rawType == null) {
                throw new IllegalStateException("Cannot transform @New when there is no known raw type");
            } else if (newQualifier.value().equals(New.class)) {
                qualifier = new NewLiteral(rawType);
                qualifierInstance = QualifierInstance.of(qualifier, store);
            }
        }

        checkQualifier(qualifier, qualifierInstance, annotationType);
        this.qualifiers.add(qualifier);
        this.qualifierInstances.add(qualifierInstance);
        this.mappedQualifiers.put(annotationType, qualifier);
        return this;
    }

    public ResolvableBuilder addQualifierIfAbsent(Annotation qualifier) {
        if (!qualifiers.contains(qualifier)) {
            addQualifier(qualifier);
        }
        return this;
    }

    public ResolvableBuilder addQualifiers(Annotation[] qualifiers) {
        for (Annotation qualifier : qualifiers) {
            addQualifier(qualifier);
        }
        return this;
    }

    public ResolvableBuilder addQualifiers(Collection<Annotation> qualifiers) {
        for (Annotation qualifier : qualifiers) {
            addQualifier(qualifier);
        }
        return this;
    }

    protected void checkQualifier(Annotation qualifier, final QualifierInstance qualifierInstance, Class<? extends Annotation> annotationType) {
        if (!store.getBindingTypeModel(annotationType).isValid()) {
            throw BeanManagerLogger.LOG.invalidQualifier(qualifier);
        }
        if (qualifierInstances.contains(qualifierInstance)) {
            throw BeanManagerLogger.LOG.duplicateQualifiers(qualifiers);
        }
    }

    protected static class ResolvableImpl implements Resolvable {

        private final Set<QualifierInstance> qualifierInstances;
        private final Map<Class<? extends Annotation>, Annotation> mappedQualifiers;
        private final Set<Type> typeClosure;
        private final Class<?> rawType;
        private final Bean<?> declaringBean;
        private final boolean delegate;

        protected ResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Map<Class<? extends Annotation>, Annotation> mappedQualifiers, Bean<?> declaringBean, final Set<QualifierInstance> qualifierInstances, boolean delegate) {
            this.mappedQualifiers = mappedQualifiers;
            this.typeClosure = typeClosure;
            this.rawType = rawType;
            this.declaringBean = declaringBean;
            this.qualifierInstances = qualifierInstances;
            this.delegate = delegate;
        }

        public Set<QualifierInstance> getQualifiers() {
            return qualifierInstances;
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return mappedQualifiers.containsKey(annotationType);
        }

        public Set<Type> getTypes() {
            return typeClosure;
        }

        public boolean isAssignableTo(Class<?> clazz) {
            AssignabilityRules rules = null;
            if (isDelegate()) {
                rules = DelegateInjectionPointAssignabilityRules.instance();
            } else {
                rules = BeanTypeAssignabilityRules.instance();
            }
            return rules.isAssignableFrom(clazz, typeClosure);
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return Reflections.<A>cast(mappedQualifiers.get(annotationType));
        }

        public Class<?> getJavaClass() {
            return rawType;
        }

        public Bean<?> getDeclaringBean() {
            return declaringBean;
        }

        @Override
        public String toString() {
            return "Types: " + getTypes() + "; Bindings: " + getQualifiers();
        }

        public int hashCode() {
            int result = 17;
            result = 31 * result + this.getTypes().hashCode();
            result = 31 * result + this.qualifierInstances.hashCode();
            return result;
        }

        public boolean equals(Object o) {
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
