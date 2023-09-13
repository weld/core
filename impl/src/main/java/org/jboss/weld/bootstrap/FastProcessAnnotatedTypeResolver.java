/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.spi.ClassFileInfo;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.Reflections;

/**
 * ProcessAnnotatedType observer method resolver. It uses {@link ClassFileServices} for resolution and thus entirely avoids
 * loading the classes which speeds up
 * especially large deployments.
 *
 * Although this resolver covers most of the possible PAT observer method types, there are several cases when
 * {@link ClassFileInfo} used by this resolver is not
 * sufficient to perform observer method resolution correctly. If such observer method is present in the deployment, the
 * constructor of this class throws
 * {@link UnsupportedObserverMethodException}. This exception is expected to be caught by the deployer and observer method
 * resolution using the default
 * {@link TypeSafeObserverResolver} is performed instead.
 *
 * @author Jozef Hartinger
 *
 */
public class FastProcessAnnotatedTypeResolver extends AbstractBootstrapService {

    private static class ExactTypePredicate implements Predicate<ClassFileInfo> {
        private final Class<?> type;

        public ExactTypePredicate(Class<?> type) {
            this.type = type;
        }

        @Override
        public boolean test(ClassFileInfo input) {
            return type.getName().equals(input.getClassName());
        }
    }

    private static class AssignableToPredicate implements Predicate<ClassFileInfo> {

        private final Class<?> type;

        public AssignableToPredicate(Class<?> type) {
            this.type = type;
        }

        @Override
        public boolean test(ClassFileInfo input) {
            return input.isAssignableTo(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static class CompositePredicate implements Predicate<ClassFileInfo> {

        private static CompositePredicate assignable(Class<?>[] classes) {
            Predicate<ClassFileInfo>[] predicates = new Predicate[classes.length];
            for (int i = 0; i < classes.length; i++) {
                predicates[i] = new AssignableToPredicate(classes[i]);
            }
            return new CompositePredicate(predicates);
        }

        private final Predicate<ClassFileInfo>[] predicates;

        public CompositePredicate(Predicate<ClassFileInfo>[] predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean test(ClassFileInfo input) {
            for (Predicate<ClassFileInfo> predicate : predicates) {
                if (!predicate.test(input)) {
                    return false;
                }
            }
            return true;
        }
    }

    private final Set<ContainerLifecycleEventObserverMethod<?>> catchAllObservers;
    private final Map<ContainerLifecycleEventObserverMethod<?>, Predicate<ClassFileInfo>> observers;

    public FastProcessAnnotatedTypeResolver(Iterable<ObserverMethod<?>> observers) throws UnsupportedObserverMethodException {
        this.catchAllObservers = new HashSet<>();
        this.observers = new LinkedHashMap<ContainerLifecycleEventObserverMethod<?>, Predicate<ClassFileInfo>>();
        for (ObserverMethod<?> o : observers) {
            if (o instanceof ContainerLifecycleEventObserverMethod<?>) {
                final Set<Annotation> qualifiers = o.getObservedQualifiers();
                // only process observer methods with no qualifiers or with @Any
                if (qualifiers.isEmpty()
                        || (qualifiers.size() == 1 && Any.class.equals(qualifiers.iterator().next().annotationType()))) {
                    process((ContainerLifecycleEventObserverMethod<?>) o, o.getObservedType());
                }
            }
        }
    }

    private void process(ContainerLifecycleEventObserverMethod<?> observer, Type observedType)
            throws UnsupportedObserverMethodException {
        if (Object.class.equals(observedType)) {
            // void observe(Object event)
            catchAllObservers.add(observer);
        } else if (ProcessAnnotatedType.class.equals(observedType)) {
            // void observe(ProcessAnnotatedType event)
            catchAllObservers.add(observer);
        } else if (observedType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) observedType;
            if (ProcessAnnotatedType.class.equals(type.getRawType())) {
                Type typeParameter = type.getActualTypeArguments()[0];
                if (typeParameter instanceof Class<?>) {
                    this.observers.put(observer, new ExactTypePredicate(Reflections.getRawType(typeParameter)));
                } else if (typeParameter instanceof ParameterizedType) {
                    /*
                     * The event type always has the form of ProcessAnnotatedType<X> where X is a raw type.
                     * Therefore, no event will ever match an observer with type ProcessAnnotatedType<Foo<Y>> no matter
                     * what Y is. This would be because primarily because parameterized are invariant. Event for an exact match
                     * of the raw type, Foo raw event type is not assignable to Foo<?> parameterized type according to CDI
                     * assignability rules.
                     */
                    return;
                } else if (typeParameter instanceof WildcardType) {
                    // void observe(ProcessAnnotatedType<?> event)
                    WildcardType wildCard = (WildcardType) typeParameter;
                    checkBounds(observer, wildCard.getUpperBounds());
                    this.observers.put(observer, CompositePredicate.assignable(Types.getRawTypes(wildCard.getUpperBounds())));
                } else if (typeParameter instanceof TypeVariable<?>) {
                    // <T> void observe(ProcessAnnotatedType<T> event)
                    TypeVariable<?> variable = (TypeVariable<?>) typeParameter;
                    checkBounds(observer, variable.getBounds());
                    this.observers.put(observer, CompositePredicate.assignable(Types.getRawTypes(variable.getBounds())));
                }
            }
        } else if (observedType instanceof TypeVariable<?>) {
            defaultRules(observer, observedType);
        }
    }

    private void checkBounds(ContainerLifecycleEventObserverMethod<?> observer, Type[] bounds)
            throws UnsupportedObserverMethodException {
        for (Type type : bounds) {
            if (!(type instanceof Class<?>)) {
                throw new UnsupportedObserverMethodException(observer);
            }
        }
    }

    private void defaultRules(ContainerLifecycleEventObserverMethod<?> observer, Type observedType)
            throws UnsupportedObserverMethodException {
        if (ProcessAnnotatedType.class.equals(observedType)) {
            catchAllObservers.add(observer);
        } else if (observedType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) observedType;
            if (ProcessAnnotatedType.class.equals(parameterizedType.getRawType())) {
                Type argument = parameterizedType.getActualTypeArguments()[0];
                if (argument instanceof Class<?>) {
                    this.observers.put(observer, new ExactTypePredicate(Reflections.getRawType(argument)));
                } else {
                    throw new UnsupportedObserverMethodException(observer);
                }
            }
        } else if (observedType instanceof TypeVariable) {
            final TypeVariable<?> typeVariable = (TypeVariable<?>) observedType;
            if (Reflections.isUnboundedTypeVariable(observedType)) {
                // <T> void observe(T event)
                catchAllObservers.add(observer);
            } else {
                if (typeVariable.getBounds().length == 1) { // here we expect that a PAT impl only implements the PAT interface
                    defaultRules(observer, typeVariable.getBounds()[0]);
                }
            }
        }
    }

    /**
     * Resolves a set of {@code ProcessAnnotatedType} observer methods for the specified class. If no observer methods are
     * resolved, an
     * empty set is returned.
     *
     * @param className the specified class name
     * @return the set of resolved ProcessAnnotatedType observer methods
     */
    public Set<ContainerLifecycleEventObserverMethod<?>> resolveProcessAnnotatedTypeObservers(
            ClassFileServices classFileServices, String className) {
        Set<ContainerLifecycleEventObserverMethod<?>> result = new HashSet<ContainerLifecycleEventObserverMethod<?>>();
        result.addAll(catchAllObservers);

        ClassFileInfo classInfo = classFileServices.getClassFileInfo(className);
        for (Map.Entry<ContainerLifecycleEventObserverMethod<?>, Predicate<ClassFileInfo>> entry : observers.entrySet()) {
            ContainerLifecycleEventObserverMethod<?> observer = entry.getKey();
            if (containsRequiredAnnotation(classInfo, observer) && entry.getValue().test(classInfo)) {
                result.add(observer);
            }
        }
        return result;
    }

    private boolean containsRequiredAnnotation(ClassFileInfo classInfo, ContainerLifecycleEventObserverMethod<?> observer) {
        if (observer.getRequiredAnnotations().isEmpty()) {
            return true;
        }
        for (Class<? extends Annotation> annotation : observer.getRequiredAnnotations()) {
            if (classInfo.containsAnnotation(annotation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanupAfterBoot() {
        catchAllObservers.clear();
        observers.clear();
    }
}
