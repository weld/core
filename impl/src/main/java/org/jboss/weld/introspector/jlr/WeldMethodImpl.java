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
package org.jboss.weld.introspector.jlr;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.TypeClosureLazyValueHolder;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an annotated method
 * <p/>
 * This class is immutable and therefore threadsafe
 *
 * @param <T>
 * @author Pete Muir
 */
public class WeldMethodImpl<T, X> extends AbstractWeldCallable<T, X, Method> implements WeldMethod<T, X> {

    // The underlying method
    private final Method method;

    // The abstracted parameters
    private final ArrayList<WeldParameter<?, X>> parameters;

    // The property name
    private final String propertyName;

    private final MethodSignature signature;

    private volatile Map<Class<?>, Method> methods;

    public static <T, X> WeldMethodImpl<T, X> of(String contextId, Method method, WeldClass<X> declaringClass, ClassTransformer classTransformer) {
        return new WeldMethodImpl<T, X>(contextId, method, Reflections.<Class<T>>cast(method.getReturnType()), method.getGenericReturnType(), new TypeClosureLazyValueHolder(contextId, method.getGenericReturnType()), null, buildAnnotationMap(method.getAnnotations()), buildAnnotationMap(method.getDeclaredAnnotations()), declaringClass, classTransformer);
    }

    public static <T, X> WeldMethodImpl<T, X> of(String contextId, AnnotatedMethod<? super X> method, WeldClass<X> declaringClass, ClassTransformer classTransformer) {
        return new WeldMethodImpl<T, X>(contextId, method.getJavaMember(), Reflections.<Class<T>>cast(method.getJavaMember().getReturnType()), method.getBaseType(), new TypeClosureLazyValueHolder(contextId, method.getTypeClosure()), method, buildAnnotationMap(method.getAnnotations()), buildAnnotationMap(method.getAnnotations()), declaringClass, classTransformer);
    }

    /**
     * Constructor
     * <p/>
     * Initializes the superclass with the built annotation map, sets the method
     * and declaring class abstraction and detects the actual type arguments
     *
     * @param method         The underlying method
     * @param declaringClass The declaring class abstraction
     */
    private WeldMethodImpl(String contextId, Method method, final Class<T> rawType, final Type type, LazyValueHolder<Set<Type>> typeClosure, AnnotatedMethod<? super X> annotatedMethod, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, WeldClass<X> declaringClass, ClassTransformer classTransformer) {
        super(contextId, annotationMap, declaredAnnotationMap, classTransformer, method, rawType, type, typeClosure, declaringClass);
        this.method = method;
        this.parameters = new ArrayList<WeldParameter<?, X>>(method.getParameterTypes().length);
        this.methods = Collections.<Class<?>, Method>singletonMap(method.getDeclaringClass(), method);

        if (annotatedMethod == null) {
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                if (method.getParameterAnnotations()[i].length > 0) {
                    Class<? extends Object> clazz = method.getParameterTypes()[i];
                    Type parametertype = method.getGenericParameterTypes()[i];
                    WeldParameter<?, X> parameter = WeldParameterImpl.of(contextId, method.getParameterAnnotations()[i], clazz, parametertype, this, i, classTransformer);
                    this.parameters.add(parameter);
                } else {
                    Class<? extends Object> clazz = method.getParameterTypes()[i];
                    Type parameterType = method.getGenericParameterTypes()[i];
                    WeldParameter<?, X> parameter = WeldParameterImpl.of(contextId, new Annotation[0], Reflections.<Class<Object>>cast(clazz), parameterType, this, i, classTransformer);
                    this.parameters.add(parameter);
                }
            }
        } else {
            if (annotatedMethod.getParameters().size() != method.getParameterTypes().length) {
                throw new DefinitionException(ReflectionMessage.INCORRECT_NUMBER_OF_ANNOTATED_PARAMETERS_METHOD, annotatedMethod.getParameters().size(), annotatedMethod, annotatedMethod.getParameters(), Arrays.asList(method.getParameterTypes()));
            } else {
                for (AnnotatedParameter<? super X> annotatedParameter : annotatedMethod.getParameters()) {
                    WeldParameter<?, X> parameter = WeldParameterImpl.of(contextId, annotatedParameter.getAnnotations(), method.getParameterTypes()[annotatedParameter.getPosition()], annotatedParameter.getBaseType(), this, annotatedParameter.getPosition(), classTransformer);
                    this.parameters.add(parameter);
                }
            }

        }
        this.parameters.trimToSize();

        String propertyName = Reflections.getPropertyName(getDelegate());
        if (propertyName == null) {
            this.propertyName = getName();
        } else {
            this.propertyName = propertyName;
        }
        this.signature = new MethodSignatureImpl(this);

    }

    @Override
    public Method getDelegate() {
        return method;
    }

    public List<WeldParameter<?, X>> getWeldParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public Class<?>[] getParameterTypesAsArray() {
        return method.getParameterTypes();
    }

    public List<WeldParameter<?, X>> getWeldParameters(Class<? extends Annotation> annotationType) {
        List<WeldParameter<?, X>> ret = new ArrayList<WeldParameter<?, X>>();
        for (WeldParameter<?, X> parameter : parameters) {
            if (parameter.isAnnotationPresent(annotationType)) {
                ret.add(parameter);
            }
        }
        return ret;
    }

    public boolean isEquivalent(Method method) {
        return this.getDeclaringType().isEquivalent(method.getDeclaringClass()) && this.getName().equals(method.getName()) && Arrays.equals(this.getParameterTypesAsArray(), method.getParameterTypes());
    }


    public T invokeOnInstance(Object instance, Object... parameters) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Map<Class<?>, Method> methods = this.methods;
        Method method = methods.get(instance.getClass());
        if (method == null) {
            //the same method may be written to the map twice, but that is ok
            //lookupMethod is very slow
            method = SecureReflections.lookupMethod(instance.getClass(), getName(), getParameterTypesAsArray());
            synchronized (this) {
                final Map<Class<?>, Method> newMethods = new HashMap<Class<?>, Method>(methods);
                newMethods.put(instance.getClass(), method);
                this.methods = Collections.unmodifiableMap(newMethods);
            }
        }
        return SecureReflections.<T>invoke(instance, method, parameters);
    }

    public T invoke(Object instance, Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return SecureReflections.<T>invoke(instance, method, parameters);
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return "[method] " + Formats.addSpaceIfNeeded(Formats.formatAnnotations(method.getAnnotations())) + Formats.addSpaceIfNeeded(Formats.formatModifiers(getJavaMember().getModifiers())) + getDeclaringType().getName() + "." + getName() + Formats.formatAsFormalParameterList(getWeldParameters());
    }

    public MethodSignature getSignature() {
        return signature;
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return Collections.unmodifiableList(Reflections.<List<AnnotatedParameter<X>>>cast(parameters));
    }

    public boolean isGeneric() {
        return getJavaMember().getTypeParameters().length > 0;
    }

}
