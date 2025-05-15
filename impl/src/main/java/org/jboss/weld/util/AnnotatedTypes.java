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
package org.jboss.weld.util;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.inject.Scope;

import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.security.GetDeclaredMethodsAction;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Class that can take an AnnotatedType and return a unique string
 * representation of that type
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
public class AnnotatedTypes {

    private static Set<Class<? extends Annotation>> BEAN_DEFINING_ANNOTATIONS = ImmutableSet.of(
            Dependent.class, RequestScoped.class, ConversationScoped.class, SessionScoped.class, ApplicationScoped.class,
            jakarta.interceptor.Interceptor.class, jakarta.decorator.Decorator.class,
            Model.class);

    private static Set<Class<? extends Annotation>> META_ANNOTATIONS = ImmutableSet.of(Stereotype.class, NormalScope.class);

    public static final Set<Class<? extends Annotation>> TRIM_META_ANNOTATIONS = ImmutableSet.of(Stereotype.class,
            NormalScope.class, Scope.class);

    /**
     * Does the first stage of comparing AnnotatedCallables, however it cannot
     * compare the method parameters
     */
    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class AnnotatedCallableComparator<T> implements Comparator<AnnotatedCallable<? super T>> {

        public int compare(AnnotatedCallable<? super T> arg0, AnnotatedCallable<? super T> arg1) {
            // compare the names first
            int result = (arg0.getJavaMember().getName().compareTo(arg1.getJavaMember().getName()));
            if (result != 0) {
                return result;
            }
            result = arg0.getJavaMember().getDeclaringClass().getName()
                    .compareTo(arg1.getJavaMember().getDeclaringClass().getName());
            if (result != 0) {
                return result;
            }
            result = arg0.getParameters().size() - arg1.getParameters().size();
            return result;
        }

    }

    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class AnnotatedMethodComparator<T> implements Comparator<AnnotatedMethod<? super T>> {

        public static <T> Comparator<AnnotatedMethod<? super T>> instance() {
            return new AnnotatedMethodComparator<T>();
        }

        private AnnotatedCallableComparator<T> callableComparator = new AnnotatedCallableComparator<T>();

        public int compare(AnnotatedMethod<? super T> arg0, AnnotatedMethod<? super T> arg1) {
            int result = callableComparator.compare(arg0, arg1);
            if (result != 0) {
                return result;
            }

            result = arg0.getJavaMember().getParameterCount() - arg1.getJavaMember().getParameterCount();
            if (result != 0) {
                return result;
            }

            for (int i = 0; i < arg0.getJavaMember().getParameterCount(); ++i) {
                Class<?> p0 = arg0.getJavaMember().getParameterTypes()[i];
                Class<?> p1 = arg1.getJavaMember().getParameterTypes()[i];
                result = p0.getName().compareTo(p1.getName());
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

    }

    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class AnnotatedConstructorComparator<T> implements Comparator<AnnotatedConstructor<? super T>> {

        public static <T> Comparator<AnnotatedConstructor<? super T>> instance() {
            return new AnnotatedConstructorComparator<T>();
        }

        private AnnotatedCallableComparator<T> callableComparator = new AnnotatedCallableComparator<T>();

        public int compare(AnnotatedConstructor<? super T> arg0, AnnotatedConstructor<? super T> arg1) {
            int result = callableComparator.compare(arg0, arg1);
            if (result != 0) {
                return result;
            }

            result = arg0.getJavaMember().getParameterCount() - arg1.getJavaMember().getParameterCount();
            if (result != 0) {
                return result;
            }

            for (int i = 0; i < arg0.getJavaMember().getParameterCount(); ++i) {
                Class<?> p0 = arg0.getJavaMember().getParameterTypes()[i];
                Class<?> p1 = arg1.getJavaMember().getParameterTypes()[i];
                result = p0.getName().compareTo(p1.getName());
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

    }

    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class AnnotatedFieldComparator<T> implements Comparator<AnnotatedField<? super T>> {

        public static <T> Comparator<AnnotatedField<? super T>> instance() {
            return new AnnotatedFieldComparator<T>();
        }

        public int compare(AnnotatedField<? super T> arg0, AnnotatedField<? super T> arg1) {
            if (arg0.getJavaMember().getName().equals(arg1.getJavaMember().getName())) {
                return arg0.getJavaMember().getDeclaringClass().getName()
                        .compareTo(arg1.getJavaMember().getDeclaringClass().getName());
            }
            return arg0.getJavaMember().getName().compareTo(arg1.getJavaMember().getName());
        }

    }

    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class AnnotationComparator implements Comparator<Annotation> {

        public static final Comparator<Annotation> INSTANCE = new AnnotationComparator();

        public int compare(Annotation arg0, Annotation arg1) {
            return arg0.annotationType().getName().compareTo(arg1.annotationType().getName());
        }
    }

    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class MethodComparator implements Comparator<Method> {

        public static final Comparator<Method> INSTANCE = new MethodComparator();

        public int compare(Method arg0, Method arg1) {
            return arg0.getName().compareTo(arg1.getName());
        }
    }

    private static final char SEPARATOR = ';';

    /**
     * Generates a unique signature for an annotated type. Members without
     * annotations are omitted to reduce the length of the signature
     *
     * @param <X>
     * @param annotatedType
     * @return hash of a signature for a concrete annotated type
     */
    public static <X> String createTypeId(AnnotatedType<X> annotatedType) {
        String id = createTypeId(annotatedType.getJavaClass(), annotatedType.getAnnotations(), annotatedType.getMethods(),
                annotatedType.getFields(), annotatedType.getConstructors());
        String hash = hash(id);
        MetadataLogger.LOG.tracef("Generated AnnotatedType id hash for %s: %s", id, hash);
        return hash;
    }

    public static String hash(String id) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(id.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates a unique signature for a concrete class
     *
     * @param <X>
     * @param annotatedType
     * @return
     */
    private static <X> String createTypeId(Class<X> clazz, Collection<Annotation> annotations,
            Collection<AnnotatedMethod<? super X>> methods, Collection<AnnotatedField<? super X>> fields,
            Collection<AnnotatedConstructor<X>> constructors) {
        StringBuilder builder = new StringBuilder();

        builder.append(clazz.getName());
        builder.append(createAnnotationCollectionId(annotations));
        builder.append("{");

        // now deal with the fields
        List<AnnotatedField<? super X>> sortedFields = new ArrayList<AnnotatedField<? super X>>();
        sortedFields.addAll(fields);
        Collections.sort(sortedFields, AnnotatedFieldComparator.<X> instance());
        for (AnnotatedField<? super X> field : sortedFields) {
            if (!field.getAnnotations().isEmpty()) {
                builder.append(createFieldId(field));
                builder.append(SEPARATOR);
            }
        }

        // methods
        List<AnnotatedMethod<? super X>> sortedMethods = new ArrayList<AnnotatedMethod<? super X>>();
        sortedMethods.addAll(methods);
        Collections.sort(sortedMethods, AnnotatedMethodComparator.<X> instance());
        for (AnnotatedMethod<? super X> method : sortedMethods) {
            if (!method.getAnnotations().isEmpty() || hasMethodParameters(method)) {
                builder.append(createCallableId(method));
                builder.append(SEPARATOR);
            }
        }

        // constructors
        List<AnnotatedConstructor<? super X>> sortedConstructors = new ArrayList<AnnotatedConstructor<? super X>>();
        sortedConstructors.addAll(constructors);
        Collections.sort(sortedConstructors, AnnotatedConstructorComparator.<X> instance());
        for (AnnotatedConstructor<? super X> constructor : sortedConstructors) {
            if (!constructor.getAnnotations().isEmpty() || hasMethodParameters(constructor)) {
                builder.append(createCallableId(constructor));
                builder.append(SEPARATOR);
            }
        }
        builder.append("}");

        return builder.toString();
    }

    private static <X> boolean hasMethodParameters(AnnotatedCallable<X> callable) {
        for (AnnotatedParameter<X> parameter : callable.getParameters()) {
            if (!parameter.getAnnotations().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected static String createAnnotationCollectionId(Collection<Annotation> annotations) {
        if (annotations.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append('[');

        List<Annotation> annotationList = new ArrayList<Annotation>(annotations.size());
        annotationList.addAll(annotations);
        Collections.sort(annotationList, AnnotationComparator.INSTANCE);

        for (Annotation a : annotationList) {
            builder.append('@');
            builder.append(a.annotationType().getName());
            builder.append('(');
            Method[] declaredMethods = AccessController.doPrivileged(new GetDeclaredMethodsAction(a.annotationType()));
            List<Method> methods = new ArrayList<Method>(declaredMethods.length);
            Collections.addAll(methods, declaredMethods);
            Collections.sort(methods, MethodComparator.INSTANCE);

            for (int i = 0; i < methods.size(); ++i) {
                Method method = methods.get(i);
                try {
                    Object value = method.invoke(a);
                    builder.append(method.getName());
                    builder.append('=');
                    // we need to detect arrays and write out their contents, plain toString() won't work there
                    if (value instanceof Object[]) {
                        builder.append(Arrays.toString((Object[]) value));
                    } else if (value instanceof byte[]) {
                        builder.append(Arrays.toString((byte[]) value));
                    } else if (value instanceof short[]) {
                        builder.append(Arrays.toString((short[]) value));
                    } else if (value instanceof int[]) {
                        builder.append(Arrays.toString((int[]) value));
                    } else if (value instanceof long[]) {
                        builder.append(Arrays.toString((long[]) value));
                    } else if (value instanceof float[]) {
                        builder.append(Arrays.toString((float[]) value));
                    } else if (value instanceof double[]) {
                        builder.append(Arrays.toString((double[]) value));
                    } else if (value instanceof boolean[]) {
                        builder.append(Arrays.toString((boolean[]) value));
                    } else if (value instanceof char[]) {
                        builder.append(Arrays.toString((char[]) value));
                    } else {
                        builder.append(value.toString());
                    }
                } catch (NullPointerException e) {
                    throwRE(a, method, e);
                } catch (IllegalArgumentException e) {
                    throwRE(a, method, e);
                } catch (IllegalAccessException e) {
                    throwRE(a, method, e);
                } catch (InvocationTargetException e) {
                    throwRE(a, method, e);
                }
                if (i + 1 != methods.size()) {
                    builder.append(',');
                }
            }
            builder.append(')');
        }
        builder.append(']');
        return builder.toString();
    }

    private static void throwRE(Annotation a, Method method, Throwable e) {
        throw new RuntimeException(e.getClass().getSimpleName() + " accessing annotation member, annotation: "
                + a.annotationType().getName() + " member: " + method.getName(), e);
    }

    public static <X> String createFieldId(AnnotatedField<X> field) {
        return createFieldId(field.getJavaMember(), field.getAnnotations());
    }

    public static <X> String createFieldId(Field field, Collection<Annotation> annotations) {
        StringBuilder builder = new StringBuilder();
        builder.append(field.getDeclaringClass().getName());
        builder.append('.');
        builder.append(field.getName());
        builder.append(createAnnotationCollectionId(annotations));
        return builder.toString();
    }

    public static <X> String createCallableId(AnnotatedCallable<X> method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getJavaMember().getDeclaringClass().getName());
        builder.append('.');
        builder.append(method.getJavaMember().getName());
        builder.append(createAnnotationCollectionId(method.getAnnotations()));
        builder.append(createParameterListId(method.getParameters()));
        return builder.toString();
    }

    public static <X> String createMethodId(Method method, Set<Annotation> annotations,
            List<AnnotatedParameter<X>> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName());
        builder.append('.');
        builder.append(method.getName());
        builder.append(createAnnotationCollectionId(annotations));
        builder.append(createParameterListId(parameters));
        return builder.toString();
    }

    public static <X> String createConstructorId(Constructor<X> constructor, Set<Annotation> annotations,
            List<AnnotatedParameter<X>> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(constructor.getDeclaringClass().getName());
        builder.append('.');
        builder.append(constructor.getName());
        builder.append(createAnnotationCollectionId(annotations));
        builder.append(createParameterListId(parameters));
        return builder.toString();
    }

    public static <X> String createParameterListId(List<AnnotatedParameter<X>> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < parameters.size(); ++i) {
            AnnotatedParameter<X> ap = parameters.get(i);
            builder.append(createParameterId(ap));
            if (i + 1 != parameters.size()) {
                builder.append(',');
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static <X> String createParameterId(AnnotatedParameter<X> annotatedParameter) {
        return createParameterId(annotatedParameter.getBaseType(), annotatedParameter.getAnnotations());
    }

    public static <X> String createParameterId(Type type, Set<Annotation> annotations) {
        StringBuilder builder = new StringBuilder();
        if (type instanceof Class<?>) {
            Class<?> c = (Class<?>) type;
            builder.append(c.getName());
        } else {
            builder.append(type.toString());
        }
        builder.append(createAnnotationCollectionId(annotations));
        return builder.toString();
    }

    /**
     * compares two annotated elements to see if they have the same annotations
     *
     * @param a1
     * @param a2
     * @return
     */
    private static boolean compareAnnotated(Annotated a1, Annotated a2) {
        return a1.getAnnotations().equals(a2.getAnnotations());
    }

    /**
     * compares two annotated elements to see if they have the same annotations
     */
    private static boolean compareAnnotatedParameters(List<? extends AnnotatedParameter<?>> p1,
            List<? extends AnnotatedParameter<?>> p2) {
        if (p1.size() != p2.size()) {
            return false;
        }
        for (int i = 0; i < p1.size(); ++i) {
            if (!compareAnnotated(p1.get(i), p2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two annotated parameters and returns true if they are equal
     */
    public static boolean compareAnnotatedParameters(AnnotatedParameter<?> p1, AnnotatedParameter<?> p2) {
        return compareAnnotatedCallable(p1.getDeclaringCallable(), p2.getDeclaringCallable())
                && p1.getPosition() == p2.getPosition() && compareAnnotated(p1, p2);
    }

    public static boolean compareAnnotatedField(AnnotatedField<?> f1, AnnotatedField<?> f2) {
        if (!f1.getJavaMember().equals(f2.getJavaMember())) {
            return false;
        }
        return compareAnnotated(f1, f2);
    }

    public static boolean compareAnnotatedCallable(AnnotatedCallable<?> m1, AnnotatedCallable<?> m2) {
        if (!m1.getJavaMember().equals(m2.getJavaMember())) {
            return false;
        }
        if (!compareAnnotated(m1, m2)) {
            return false;
        }
        return compareAnnotatedParameters(m1.getParameters(), m2.getParameters());
    }

    /**
     * Compares two annotated types and returns true if they are the same
     */
    public static boolean compareAnnotatedTypes(AnnotatedType<?> t1, AnnotatedType<?> t2) {
        if (!t1.getJavaClass().equals(t2.getJavaClass())) {
            return false;
        }
        if (!compareAnnotated(t1, t2)) {
            return false;
        }

        if (t1.getFields().size() != t2.getFields().size()) {
            return false;
        }
        Map<Field, AnnotatedField<?>> fields = new HashMap<Field, AnnotatedField<?>>();
        for (AnnotatedField<?> f : t2.getFields()) {
            fields.put(f.getJavaMember(), f);
        }
        for (AnnotatedField<?> f : t1.getFields()) {
            if (fields.containsKey(f.getJavaMember())) {
                if (!compareAnnotatedField(f, fields.get(f.getJavaMember()))) {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (t1.getMethods().size() != t2.getMethods().size()) {
            return false;
        }
        Map<Method, AnnotatedMethod<?>> methods = new HashMap<Method, AnnotatedMethod<?>>();
        for (AnnotatedMethod<?> f : t2.getMethods()) {
            methods.put(f.getJavaMember(), f);
        }
        for (AnnotatedMethod<?> f : t1.getMethods()) {
            if (methods.containsKey(f.getJavaMember())) {
                if (!compareAnnotatedCallable(f, methods.get(f.getJavaMember()))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (t1.getConstructors().size() != t2.getConstructors().size()) {
            return false;
        }
        Map<Constructor<?>, AnnotatedConstructor<?>> constructors = new HashMap<Constructor<?>, AnnotatedConstructor<?>>();
        for (AnnotatedConstructor<?> f : t2.getConstructors()) {
            constructors.put(f.getJavaMember(), f);
        }
        for (AnnotatedConstructor<?> f : t1.getConstructors()) {
            if (constructors.containsKey(f.getJavaMember())) {
                if (!compareAnnotatedCallable(f, constructors.get(f.getJavaMember()))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;

    }

    /**
     * Returns the declaring {@link AnnotatedType} of a given annotated.
     *
     * For an {@link AnnotatedMember}, {@link AnnotatedMember#getDeclaringType()} is returned.
     * For an {@link AnnotatedParameter}, the declaring annotated type of {@link AnnotatedParameter#getDeclaringCallable()} is
     * returned.
     * If the parameter is an {@link AnnotatedType}, it is returned.
     *
     * @throws IllegalArgumentException if the annotated parameter is an unknown non-standard {@link Annotated} subclass.
     */
    public static AnnotatedType<?> getDeclaringAnnotatedType(Annotated annotated) {
        if (annotated == null) {
            throw new IllegalArgumentException("Annotated cannot be null");
        }
        if (annotated instanceof AnnotatedType<?>) {
            return cast(annotated);
        }
        if (annotated instanceof AnnotatedMember<?>) {
            return Reflections.<AnnotatedMember<?>> cast(annotated).getDeclaringType();
        }
        if (annotated instanceof AnnotatedParameter<?>) {
            return getDeclaringAnnotatedType(Reflections.<AnnotatedParameter<?>> cast(annotated).getDeclaringCallable());
        }
        throw new IllegalArgumentException("Unrecognized annotated " + annotated);
    }

    private AnnotatedTypes() {
    }

    public static boolean hasBeanDefiningAnnotation(AnnotatedType<?> annotatedType) {
        return hasBeanDefiningAnnotation(annotatedType, META_ANNOTATIONS);
    }

    public static boolean hasBeanDefiningAnnotation(AnnotatedType<?> annotatedType,
            Set<Class<? extends Annotation>> metaAnnotations) {
        for (Class<? extends Annotation> beanDefiningAnnotation : BEAN_DEFINING_ANNOTATIONS) {
            if (annotatedType.isAnnotationPresent(beanDefiningAnnotation)) {
                return true;
            }
        }
        for (Class<? extends Annotation> metaAnnotation : metaAnnotations) {
            // The check is not perfomed recursively as bean defining annotations must be declared directly on a bean class
            if (hasBeanDefiningMetaAnnotationSpecified(annotatedType.getAnnotations(), metaAnnotation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBeanDefiningMetaAnnotationSpecified(Set<Annotation> annotations,
            Class<? extends Annotation> metaAnnotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotationType)) {
                return true;
            }
        }
        return false;
    }

}
