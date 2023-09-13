/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.annotatedType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.inject.Inject;

import org.jboss.weld.tests.extensions.annotatedType.EcoFriendlyWashingMachine.EcoFriendlyWashingMachineLiteral;
import org.jboss.weld.util.collections.Arrays2;

public class AnnotatedTypeExtension implements Extension {

    public void addTumbleDryer(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {

        final Set<AnnotatedConstructor<TumbleDryer>> constructors = new HashSet<AnnotatedConstructor<TumbleDryer>>();
        final Set<AnnotatedField<? super TumbleDryer>> fields = new HashSet<AnnotatedField<? super TumbleDryer>>();
        final Set<AnnotatedMethod<? super TumbleDryer>> methods = new HashSet<AnnotatedMethod<? super TumbleDryer>>();

        final AnnotatedType<TumbleDryer> tumbleDryer = new AnnotatedType<TumbleDryer>() {

            public Set<AnnotatedConstructor<TumbleDryer>> getConstructors() {
                return constructors;
            }

            public Set<AnnotatedField<? super TumbleDryer>> getFields() {
                return fields;
            }

            public Set<AnnotatedMethod<? super TumbleDryer>> getMethods() {
                return methods;
            }

            // Now the easy stuff

            public Class<TumbleDryer> getJavaClass() {
                return TumbleDryer.class;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType.equals(Marker.class)) {
                    return (T) MarkerLiteral.INSTANCE;
                } else {
                    return null;
                }
            }

            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(MarkerLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return TumbleDryer.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(TumbleDryer.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (annotationType.equals(Marker.class)) {
                    return true;
                } else {
                    return false;
                }
            }

        };

        AnnotatedField<TumbleDryer> plug = new AnnotatedField<TumbleDryer>() {

            public Field getJavaMember() {
                try {
                    return TumbleDryer.class.getDeclaredField("plug");
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }

            public boolean isStatic() {
                return false;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType.equals(Inject.class)) {
                    return annotationType.cast(InjectLiteral.INSTANCE);
                } else if (annotationType.equals(Special.class)) {
                    return annotationType.cast(SpecialLiteral.INSTANCE);
                } else {
                    return null;
                }
            }

            public Set<Annotation> getAnnotations() {
                return Arrays2.asSet(InjectLiteral.INSTANCE, SpecialLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return Plug.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(Plug.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (annotationType.equals(Inject.class) || annotationType.equals(Special.class)) {
                    return true;
                } else {
                    return false;
                }
            }

            public AnnotatedType<TumbleDryer> getDeclaringType() {
                return tumbleDryer;
            }
        };
        fields.add(plug);

        final List<AnnotatedParameter<TumbleDryer>> runningTimeParameters = new ArrayList<AnnotatedParameter<TumbleDryer>>();
        final AnnotatedMethod<TumbleDryer> runningTimeMethod = new AnnotatedMethod<TumbleDryer>() {

            public Method getJavaMember() {
                try {
                    return TumbleDryer.class.getDeclaredMethod("setRunningTime", RunningTime.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            public List<AnnotatedParameter<TumbleDryer>> getParameters() {
                return runningTimeParameters;
            }

            public AnnotatedType<TumbleDryer> getDeclaringType() {
                return tumbleDryer;
            }

            public boolean isStatic() {
                return false;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType.equals(Inject.class)) {
                    return annotationType.cast(InjectLiteral.INSTANCE);
                } else {
                    return null;
                }
            }

            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(InjectLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return TumbleDryer.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(TumbleDryer.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (annotationType.equals(Inject.class)) {
                    return true;
                } else {
                    return false;
                }
            }

        };
        methods.add(runningTimeMethod);

        final AnnotatedParameter<TumbleDryer> runningTimeParameter = new AnnotatedParameter<TumbleDryer>() {

            public AnnotatedCallable<TumbleDryer> getDeclaringCallable() {
                return runningTimeMethod;
            }

            public int getPosition() {
                return 0;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType.equals(Special.class)) {
                    return annotationType.cast(SpecialLiteral.INSTANCE);
                } else {
                    return null;
                }
            }

            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(SpecialLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return RunningTime.class;
            }

            public Set<Type> getTypeClosure() {
                return Collections.<Type> singleton(RunningTime.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (annotationType.equals(Special.class)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        runningTimeParameters.add(runningTimeParameter);

        final List<AnnotatedParameter<TumbleDryer>> clothesParameters = new ArrayList<AnnotatedParameter<TumbleDryer>>();
        final AnnotatedConstructor<TumbleDryer> clothesConstructor = new AnnotatedConstructor<TumbleDryer>() {

            public Constructor<TumbleDryer> getJavaMember() {
                try {
                    return TumbleDryer.class.getDeclaredConstructor(Clothes.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            public List<AnnotatedParameter<TumbleDryer>> getParameters() {
                return clothesParameters;
            }

            public AnnotatedType<TumbleDryer> getDeclaringType() {
                return tumbleDryer;
            }

            public boolean isStatic() {
                return false;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType.equals(Inject.class)) {
                    return annotationType.cast(InjectLiteral.INSTANCE);
                } else {
                    return null;
                }
            }

            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(InjectLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return TumbleDryer.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(TumbleDryer.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (annotationType.equals(Inject.class)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        constructors.add(clothesConstructor);

        AnnotatedParameter<TumbleDryer> clothesParameter = new AnnotatedParameter<TumbleDryer>() {

            public AnnotatedCallable<TumbleDryer> getDeclaringCallable() {
                return clothesConstructor;
            }

            public int getPosition() {
                return 0;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType.equals(Special.class)) {
                    return annotationType.cast(SpecialLiteral.INSTANCE);
                } else {
                    return null;
                }
            }

            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(SpecialLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return Clothes.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(Clothes.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (annotationType.equals(Special.class)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        clothesParameters.add(clothesParameter);

        beforeBeanDiscovery.addAnnotatedType(tumbleDryer, TumbleDryer.class.getSimpleName());
    }

    /**
     * Adds an eco friendly wasing machine
     *
     * @param beforeBeanDiscovery
     */
    public void addWashingMachine(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        final Set<AnnotatedConstructor<WashingMachine>> constructors = new HashSet<AnnotatedConstructor<WashingMachine>>();
        final AnnotatedType<WashingMachine> type = new AnnotatedType<WashingMachine>() {

            public Set<AnnotatedConstructor<WashingMachine>> getConstructors() {
                return constructors;
            }

            public Set<AnnotatedField<? super WashingMachine>> getFields() {
                return Collections.emptySet();
            }

            public Class<WashingMachine> getJavaClass() {
                return WashingMachine.class;
            }

            public Set<AnnotatedMethod<? super WashingMachine>> getMethods() {
                return Collections.emptySet();
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (annotationType == EcoFriendlyWashingMachine.class) {
                    return annotationType.cast(EcoFriendlyWashingMachineLiteral.INSTANCE);
                }
                return null;
            }

            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(EcoFriendlyWashingMachineLiteral.INSTANCE);
            }

            public Type getBaseType() {
                return WashingMachine.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(WashingMachine.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return annotationType == EcoFriendlyWashingMachine.class;
            }

        };

        final AnnotatedConstructor<WashingMachine> constructor = new AnnotatedConstructor<WashingMachine>() {

            public Constructor<WashingMachine> getJavaMember() {
                try {
                    return WashingMachine.class.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            public List<AnnotatedParameter<WashingMachine>> getParameters() {
                return Collections.emptyList();
            }

            public AnnotatedType<WashingMachine> getDeclaringType() {
                return type;
            }

            public boolean isStatic() {
                return false;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                return null;
            }

            public Set<Annotation> getAnnotations() {
                return Collections.emptySet();
            }

            public Type getBaseType() {
                return WashingMachine.class;
            }

            public Set<Type> getTypeClosure() {
                return Arrays2.<Type> asSet(WashingMachine.class, Object.class);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return false;
            }
        };
        constructors.add(constructor);

        beforeBeanDiscovery.addAnnotatedType(type, WashingMachine.class.getSimpleName());
    }

    public void vetoOriginalTumbleDryer(@Observes ProcessAnnotatedType<TumbleDryer> event) {
        if (!event.getAnnotatedType().isAnnotationPresent(Marker.class)) {
            event.veto();
        }
    }

}
