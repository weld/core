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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.inject.Provider;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.reflection.Reflections;

import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_QUALIFIERS;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INVALID_QUALIFIER;
import static org.jboss.weld.logging.messages.ResolutionMessage.CANNOT_EXTRACT_RAW_TYPE;

public class ResolvableBuilder {

    protected Class<?> rawType;
    protected final Set<Type> types;
    protected final Set<Annotation> qualifiers;
    protected final Set<QualifierInstance> qualifierInstances;
    protected final Map<Class<? extends Annotation>, Annotation> mappedQualifiers;
    protected Bean<?> declaringBean;
    protected final BeanManagerImpl beanManager;
    private final MetaAnnotationStore store;

    public ResolvableBuilder(final BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.store = beanManager.getServices().get(MetaAnnotationStore.class);
        this.types = new HashSet<Type>();
        this.qualifiers = new HashSet<Annotation>();
        this.mappedQualifiers = new HashMap<Class<? extends Annotation>, Annotation>();
        this.qualifierInstances = new HashSet<QualifierInstance>();
    }

    public ResolvableBuilder(Type type, final BeanManagerImpl manager) {
        this(manager);
        if (type != null) {
            this.rawType = Reflections.getRawType(type);
            if (rawType == null) {
                throw new IllegalArgumentException(CANNOT_EXTRACT_RAW_TYPE, type);
            }
            this.types.add(type);
        }
    }

    public ResolvableBuilder(InjectionPoint injectionPoint, final BeanManagerImpl manager) {
        this(injectionPoint.getType(), manager);
        addQualifiers(injectionPoint.getQualifiers());
        if (mappedQualifiers.containsKey(Named.class) && injectionPoint.getMember() instanceof Field) {
            Named named = (Named) mappedQualifiers.get(Named.class);
            QualifierInstance qualifierInstance = new QualifierInstance(named, store);
            if (named.value().equals("")) {
                qualifiers.remove(named);
                qualifierInstances.remove(qualifierInstance);
                // This is field injection point with an @Named qualifier, with no value specified, we need to assume the name of the field is the value
                named = new NamedLiteral(injectionPoint.getMember().getName());
                qualifierInstance = new QualifierInstance(named, store);
                qualifiers.add(named);
                qualifierInstances.add(qualifierInstance);
                mappedQualifiers.put(Named.class, named);
            }
        }
        setDeclaringBean(injectionPoint.getBean());
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

    public Resolvable create() {
        if (qualifiers.size() == 0) {
            this.qualifierInstances.add(new QualifierInstance(DefaultLiteral.INSTANCE, store));
        }
        if (Reflections.isAssignableFrom(Event.class, types)) {
            return createFacade(Event.class);
        } else if (Reflections.isAssignableFrom(Instance.class, types)) {
            return createFacade(Instance.class);
        } else if (Reflections.isAssignableFrom(Provider.class, types)) {
            return createFacade(Provider.class);
        } else {
            return new ResolvableImpl(rawType, types, mappedQualifiers, declaringBean, qualifierInstances);
        }
    }

    private Resolvable createFacade(Class<?> rawType) {
        Set<QualifierInstance> qualifiers = Collections.<QualifierInstance>singleton(new QualifierInstance(AnyLiteral.INSTANCE, store));
        Set<Type> types = Collections.<Type>singleton(rawType);
        return new ResolvableImpl(rawType, types, mappedQualifiers, declaringBean, qualifiers);
    }

    public ResolvableBuilder addQualifier(Annotation qualifier) {
        // Handle the @New qualifier special case
        QualifierInstance qualifierInstance = new QualifierInstance(qualifier, store);
        final Class<? extends Annotation> annotationType = qualifierInstance.getAnnotationClass();
        if (annotationType.equals(New.class)) {
            New newQualifier = New.class.cast(qualifier);
            if (newQualifier.value().equals(New.class) && rawType == null) {
                throw new IllegalStateException("Cannot transform @New when there is no known raw type");
            } else if (newQualifier.value().equals(New.class)) {
                qualifier = new NewLiteral() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Class<?> value() {
                        return rawType;
                    }

                };
                qualifierInstance = new QualifierInstance(qualifier, store);
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

    public ResolvableBuilder addQualifiers(Set<Annotation> qualifiers) {
        for (Annotation qualifier : qualifiers) {
            addQualifier(qualifier);
        }
        return this;
    }

    protected void checkQualifier(Annotation qualifier, final QualifierInstance qualifierInstance, Class<? extends Annotation> annotationType) {
        if (!store.getBindingTypeModel(annotationType).isValid()) {
            throw new IllegalArgumentException(INVALID_QUALIFIER, qualifier);
        }
        if (qualifierInstances.contains(qualifierInstance)) {
            throw new IllegalArgumentException(DUPLICATE_QUALIFIERS, qualifiers);
        }
    }

    protected static class ResolvableImpl implements Resolvable {

        private final Set<QualifierInstance> qualifierInstances;
        private final Map<Class<? extends Annotation>, Annotation> mappedQualifiers;
        private final Set<Type> typeClosure;
        private final Class<?> rawType;
        private final Bean<?> declaringBean;

        protected ResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Map<Class<? extends Annotation>, Annotation> mappedQualifiers, Bean<?> declaringBean, final Set<QualifierInstance> qualifierInstances) {
            this.mappedQualifiers = mappedQualifiers;
            this.typeClosure = typeClosure;
            this.rawType = rawType;
            this.declaringBean = declaringBean;
            this.qualifierInstances = qualifierInstances;
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
            return Reflections.isAssignableFrom(clazz, typeClosure);
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
    }

}
