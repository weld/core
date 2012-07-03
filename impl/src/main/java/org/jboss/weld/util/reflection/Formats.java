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
package org.jboss.weld.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;

/**
 * Utility class to produce friendly names e.g. for debugging
 *
 * @author Pete Muir
 * @author Nicklas Karlsson
 * @author Jozef Hartinger
 */
public class Formats {

    /**
     * A transformation from one object to a String.
     *
     * @param <F> the type of the function input
     */
    private interface Function<F> {
        /**
         * Applies the function to an object of type {@code F}, resulting in an
         * object of type String.
         *
         * @param from     the source object
         * @param position the position in the list the object is at
         * @return the resulting object
         */
        String apply(F from, int position);

    }

    private static final Function<?> SPACE_DELIMITER_FUNCTION = new Function<Object>() {

        public String apply(Object from, int position) {
            if (position > 0) {
                return " " + (from == null ? "null" : from.toString());
            } else {
                return from == null ? "null" : from.toString();
            }
        }
    };

    private static final Function<?> COMMA_DELIMITER_FUNCTION = new Function<Object>() {

        public String apply(Object from, int position) {
            if (position > 0) {
                return ", " + (from == null ? "null" : from.toString());
            } else {
                return from == null ? "null" : from.toString();
            }
        }
    };

    private static final Function<Annotation> ANNOTATION_LIST_FUNCTION = new Function<Annotation>() {

        public String apply(Annotation from, int position) {
            return spaceDelimiterFunction().apply("@" + from.annotationType().getSimpleName(), position);
        }

    };

    @SuppressWarnings("unchecked")
    private static <T> Function<T> spaceDelimiterFunction() {
        return (Function<T>) SPACE_DELIMITER_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<T> commaDelimiterFunction() {
        return (Function<T>) COMMA_DELIMITER_FUNCTION;
    }

    public static String formatType(Type baseType) {
        if (baseType == null) {
            return "null";
        } else {
            Class<?> rawType = Reflections.getRawType(baseType);
            if (rawType != null) {
                return rawType.getSimpleName() + formatActualTypeArguments(Reflections.getActualTypeArguments(baseType));
            } else {
                return baseType.toString();
            }
        }
    }

    public static String formatTypes(Iterable<? extends Type> baseTypes) {
        return formatIterable(baseTypes, new Function<Type>() {

            public String apply(Type from, int position) {
                return commaDelimiterFunction().apply(formatType(from), position);
            }

        });
    }

    public static String formatBusinessInterfaceDescriptors(Iterable<? extends BusinessInterfaceDescriptor<?>> businessInterfaceDescriptors) {
        return formatIterable(businessInterfaceDescriptors, new Function<BusinessInterfaceDescriptor<?>>() {

            public String apply(BusinessInterfaceDescriptor<?> from, int position) {
                return commaDelimiterFunction().apply(formatType(from.getInterface()), position);
            }

        });
    }

    public static String addSpaceIfNeeded(String string) {
        if (string.length() > 0) {
            return string + " ";
        } else {
            return string;
        }
    }

    public static String formatAsFormalParameterList(Iterable<? extends AnnotatedParameter<?>> parameters) {
        return "(" + formatIterable(parameters, new Function<AnnotatedParameter<?>>() {

            public String apply(AnnotatedParameter<?> from, int position) {
                return commaDelimiterFunction().apply(formatParameter(from), position);
            }

        }) + ")";
    }

    public static String formatParameter(AnnotatedParameter<?> parameter) {
        return addSpaceIfNeeded(formatAnnotations(parameter.getAnnotations())) + formatType(parameter.getBaseType());
    }

    public static String formatModifiers(int modifiers) {
        return formatIterable(parseModifiers(modifiers), spaceDelimiterFunction());
    }

    private static <F> String formatIterable(Iterable<? extends F> items, Function<F> function) {
        if (items == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (F item : items) {
            stringBuilder.append(function.apply(item, i));
            i++;
        }
        return stringBuilder.toString();
    }

    private static <F> String formatIterable(F[] items, Function<F> function) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (F item : items) {
            stringBuilder.append(function.apply(item, i));
            i++;
        }
        return stringBuilder.toString();
    }

    /**
     * Parses a reflection modifier to a list of string
     *
     * @param modifiers The modifier to parse
     * @return The resulting string list
     */
    private static List<String> parseModifiers(int modifiers) {
        List<String> result = new ArrayList<String>();
        if (Modifier.isPrivate(modifiers)) {
            result.add("private");
        }
        if (Modifier.isProtected(modifiers)) {
            result.add("protected");
        }
        if (Modifier.isPublic(modifiers)) {
            result.add("public");
        }
        if (Modifier.isAbstract(modifiers)) {
            result.add("abstract");
        }
        if (Modifier.isFinal(modifiers)) {
            result.add("final");
        }
        if (Modifier.isNative(modifiers)) {
            result.add("native");
        }
        if (Modifier.isStatic(modifiers)) {
            result.add("static");
        }
        if (Modifier.isStrict(modifiers)) {
            result.add("strict");
        }
        if (Modifier.isSynchronized(modifiers)) {
            result.add("synchronized");
        }
        if (Modifier.isTransient(modifiers)) {
            result.add("transient");
        }
        if (Modifier.isVolatile(modifiers)) {
            result.add("volatile");
        }
        if (Modifier.isInterface(modifiers)) {
            result.add("interface");
        }
        return result;
    }

    public static String formatActualTypeArguments(Type type) {
        if (type instanceof ParameterizedType) {
            return formatActualTypeArguments(ParameterizedType.class.cast(type).getActualTypeArguments());
        }
        return "";
    }

    public static String formatActualTypeArguments(Type[] actualTypeArguments) {
        return wrapIfNeccessary(formatIterable(actualTypeArguments, new Function<Type>() {

            public String apply(Type from, int position) {
                return commaDelimiterFunction().apply(formatType(from), position);
            }

        }), "<", ">");
    }

    public static String wrapIfNeccessary(String string, String prepend, String append) {
        if (string != null && string.length() > 0) {
            return prepend + string + append;
        } else {
            return string;
        }
    }

    public static String formatAnnotations(Iterable<Annotation> annotations) {
        return formatIterable(annotations, ANNOTATION_LIST_FUNCTION);
    }

    /**
     * Gets a string representation from an array of annotations
     *
     * @param annotations The annotations
     * @return The string representation
     */
    public static String formatAnnotations(Annotation[] annotations) {
        return formatIterable(annotations, ANNOTATION_LIST_FUNCTION);
    }

    public static String version(Package pkg) {
        if (pkg == null) {
            throw new IllegalArgumentException("Package can not be null");
        } else {
            return version(pkg.getSpecificationVersion(), pkg.getImplementationVersion());
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE", justification = "False positive.")
    public static String version(String version, String timestamp) {
        if (version == null && timestamp != null) {
            return timestamp;
        } else if (version == null && timestamp == null) {
            return "SNAPSHOT";
        }
        String major = null;
        String minor = null;
        String micro = null;
        String qualifier = null;
        List<String> split = new ArrayList<String>(Arrays.asList(version.split("\\.")));

        String[] split2 = split.get(split.size() - 1).split("\\-");
        if (split2.length > 1) {
            // We split it, so swap out the last digit
            split.remove(split.size() - 1);
            split.add(split.size(), split2[0]);
            qualifier = split2[1];
        } else if (split2.length > 0) {
            // We didn't split it
            split.remove(split.size() - 1);
            qualifier = split2[0];
        }

        if (split.size() > 0) {
            major = split.get(0);
        }
        if (split.size() > 1) {
            minor = split.get(1);
        }
        if (split.size() > 2) {
            micro = split.get(2);
        }
        if (major == null && timestamp != null) {
            // Handle the case we only have a timestamp
            return timestamp;
        }
        if (major == null && timestamp == null) {
            // Handle the case we have nothing
            return "SNAPSHOT";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(major);
        if (minor != null) {
            builder.append(".").append(minor);
        }
        if (minor != null && micro != null) {
            builder.append(".").append(micro);
        }
        if (qualifier != null) {
            builder.append(" (");
            if (qualifier.equals("SNAPSHOT") && timestamp != null) {
                builder.append(timestamp);
            } else {
                builder.append(qualifier);
            }
            builder.append(")");
        }
        return builder.toString();
    }

    public static String formatSimpleClassName(Object object) {
        return formatSimpleClassName(object.getClass());
    }

    public static String formatSimpleClassName(Class<?> javaClass) {
        String simpleName = javaClass.getSimpleName();
        StringBuilder builder = new StringBuilder(simpleName.length() + 2);
        builder.append("[");
        builder.append(simpleName);
        builder.append("]");
        return builder.toString();
    }

    public static String formatAnnotatedType(AnnotatedType<?> type) {
        return Formats.formatSimpleClassName(type) + " " + Formats.addSpaceIfNeeded(Formats.formatModifiers(type.getJavaClass().getModifiers()))
                + Formats.formatAnnotations(type.getAnnotations()) + " class " + type.getJavaClass().getName() + Formats.formatActualTypeArguments(type.getBaseType());
    }

    public static String formatAnnotatedConstructor(AnnotatedConstructor<?> constructor) {
        return Formats.formatSimpleClassName(constructor) + " " + Formats.addSpaceIfNeeded(Formats.formatAnnotations(constructor.getAnnotations()))
                + Formats.addSpaceIfNeeded(Formats.formatModifiers(constructor.getJavaMember().getModifiers())) + constructor.getDeclaringType().getJavaClass().getName()
                + Formats.formatAsFormalParameterList(constructor.getParameters());
    }

    public static String formatAnnotatedField(AnnotatedField<?> field) {
        return Formats.formatSimpleClassName(field) + " " + Formats.addSpaceIfNeeded(Formats.formatAnnotations(field.getAnnotations()))
                + Formats.addSpaceIfNeeded(Formats.formatModifiers(field.getJavaMember().getModifiers())) + field.getDeclaringType().getJavaClass().getName() + "."
                + field.getJavaMember().getName();
    }

    public static String formatAnnotatedMethod(AnnotatedMethod<?> method) {
        return Formats.formatSimpleClassName(method) + " " + Formats.addSpaceIfNeeded(Formats.formatAnnotations(method.getAnnotations()))
                + Formats.addSpaceIfNeeded(Formats.formatModifiers(method.getJavaMember().getModifiers())) + method.getDeclaringType().getJavaClass().getName() + "."
                + method.getJavaMember().getName() + Formats.formatAsFormalParameterList(method.getParameters());
    }

    public static String formatAnnotatedParameter(AnnotatedParameter<?> parameter) {
        return Formats.formatSimpleClassName(parameter) + " Parameter " + (parameter.getPosition() + 1) + " of " + parameter.getDeclaringCallable().toString();
    }
}
