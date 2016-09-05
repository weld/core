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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.classfilewriter.util.DescriptorUtils;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;

/**
 * Utility class to produce friendly names e.g. for debugging
 *
 * @author Pete Muir
 * @author Nicklas Karlsson
 * @author Jozef Hartinger
 */
public class Formats {

    private static final String SNAPSHOT = "SNAPSHOT";
    private static final String NULL = "null";

    private static final String BCEL_CLASS_PARSER_FQCN = "com.sun.org.apache.bcel.internal.classfile.ClassParser";
    private static final String BCEL_JAVA_CLASS_FQCN = "com.sun.org.apache.bcel.internal.classfile.JavaClass";
    private static final String BCEL_METHOD_FQCN = "com.sun.org.apache.bcel.internal.classfile.Method";
    private static final String BCEL_LINE_NUMBER_TABLE_FQCN = "com.sun.org.apache.bcel.internal.classfile.LineNumberTable";
    private static final String BCEL_M_PARSE = "parse";
    private static final String BCEL_M_GET_METHODS = "getMethods";
    private static final String BCEL_M_GET_LINE_NUMBER_TABLE = "getLineNumberTable";
    private static final String BCEL_M_GET_SOURCE_LINE = "getSourceLine";
    private static final String BCEL_M_GET_NAME = "getName";
    private static final String BCEL_M_GET_MODIFIERS = "getModifiers";
    private static final String BCEL_M_GET_SIGNATURE = "getSignature";

    private static final String INIT_METHOD_NAME = "<init>";

    private Formats() {
    }

    /**
     * See also WELD-1454.
     *
     * @param ij
     * @return the formatted string
     */
    public static String formatAsStackTraceElement(InjectionPoint ij) {
        Member member;
        if (ij.getAnnotated() instanceof AnnotatedField) {
            AnnotatedField<?> annotatedField = (AnnotatedField<?>) ij.getAnnotated();
            member = annotatedField.getJavaMember();
        } else if (ij.getAnnotated() instanceof AnnotatedParameter<?>) {
            AnnotatedParameter<?> annotatedParameter = (AnnotatedParameter<?>) ij.getAnnotated();
            member = annotatedParameter.getDeclaringCallable().getJavaMember();
        } else {
            // Not throwing an exception, because this method is invoked when an exception is already being thrown.
            // Throwing an exception here would hide the original exception.
            return "-";
        }
        return Formats.formatAsStackTraceElement(member);
    }

    public static String formatAsStackTraceElement(Member member) {
        return member.getDeclaringClass().getName()
            + "." + (member instanceof Constructor<?> ? INIT_METHOD_NAME : member.getName())
            + "(" + getFileName(member.getDeclaringClass()) + ":" + getLineNumber(member) + ")";
    }

    /**
     * Try to get the line number associated with the given member.
     *
     * The reflection API does not expose such an info and so we need to analyse the bytecode. Unfortunately, it seems there is no way to get this kind of
     * information for fields. Moreover, the <code>LineNumberTable</code> attribute is just optional, i.e. the compiler is not required to store this
     * information at all. See also <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.1">Java Virtual Machine Specification</a>
     *
     * Implementation note: it wouldn't be appropriate to add a bytecode scanning dependency just for this functionality, therefore Apache BCEL included in
     * Oracle JDK 1.5+ and OpenJDK 1.6+ is used. Other JVMs should not crash as we only use it if it's on the classpath and by means of reflection calls.
     *
     * @param member
     * @param resourceLoader
     * @return the line number or 0 if it's not possible to find it
     */
    public static int getLineNumber(Member member) {

        if (!(member instanceof Method || member instanceof Constructor)) {
            // We are not able to get this info for fields
            return 0;
        }

        if (!Reflections.isClassLoadable(BCEL_JAVA_CLASS_FQCN, WeldClassLoaderResourceLoader.INSTANCE)) {
            // Apache BCEL classes not on the classpath
            return 0;
        }

        String classFile = member.getDeclaringClass().getName().replace('.', '/');
        ClassLoaderResourceLoader classFileResourceLoader = new ClassLoaderResourceLoader(member.getDeclaringClass().getClassLoader());
        InputStream in = null;

        try {
            URL classFileUrl = classFileResourceLoader.getResource(classFile + ".class");

            if (classFileUrl == null) {
                // The class file is not available
                return 0;
            }
            in = classFileUrl.openStream();

            Class<?> classParserClass = Reflections.loadClass(BCEL_CLASS_PARSER_FQCN, WeldClassLoaderResourceLoader.INSTANCE);
            Class<?> javaClassClass = Reflections.loadClass(BCEL_JAVA_CLASS_FQCN, WeldClassLoaderResourceLoader.INSTANCE);
            Class<?> methodClass = Reflections.loadClass(BCEL_METHOD_FQCN, WeldClassLoaderResourceLoader.INSTANCE);
            Class<?> lntClass = Reflections.loadClass(BCEL_LINE_NUMBER_TABLE_FQCN, WeldClassLoaderResourceLoader.INSTANCE);

            Object parser = classParserClass.getConstructor(InputStream.class, String.class).newInstance(in, classFile);
            Object javaClass = classParserClass.getMethod(BCEL_M_PARSE).invoke(parser);

            // First get all declared methods and constructors
            // Note that in bytecode constructor is translated into a method
            Object[] methods = (Object[]) javaClassClass.getMethod(BCEL_M_GET_METHODS).invoke(javaClass);
            Object match = null;

            String signature;
            String name;
            if (member instanceof Method) {
                signature = DescriptorUtils.methodDescriptor((Method) member);
                name = member.getName();
            } else if (member instanceof Constructor) {
                signature = DescriptorUtils.makeDescriptor((Constructor<?>) member);
                name = INIT_METHOD_NAME;
            } else {
                return 0;
            }

            for (Object method : methods) {
                // Matching method must have the same name, modifiers and signature
                if (methodClass.getMethod(BCEL_M_GET_NAME).invoke(method).equals(name)
                        && methodClass.getMethod(BCEL_M_GET_MODIFIERS).invoke(method).equals(member.getModifiers())
                        && methodClass.getMethod(BCEL_M_GET_SIGNATURE).invoke(method).equals(signature)) {
                    match = method;
                }
            }
            if (match != null) {
                // If a method is found, try to obtain the optional LineNumberTable attribute
                Object lineNumberTable = methodClass.getMethod(BCEL_M_GET_LINE_NUMBER_TABLE).invoke(match);
                if (lineNumberTable != null) {
                    Integer line = (Integer) lntClass.getMethod(BCEL_M_GET_SOURCE_LINE, int.class).invoke(lineNumberTable, 0);
                    return line == -1 ? 0 : line;
                }
            }
            // No suitable method found
            return 0;

        } catch (Throwable t) {
            return 0;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    return 0;
                }
            }
        }
    }

    private static String getFileName(Class<?> clazz) {
        // TODO find the actual file name the class is declared in
        return clazz.getSimpleName() + ".java";
    }

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

        @Override
        public String apply(Object from, int position) {
            if (position > 0) {
                return " " + (from == null ? NULL : from.toString());
            } else {
                return from == null ? NULL : from.toString();
            }
        }
    };

    private static final Function<?> COMMA_DELIMITER_FUNCTION = new Function<Object>() {

        @Override
        public String apply(Object from, int position) {
            if (position > 0) {
                return ", " + (from == null ? NULL : from.toString());
            } else {
                return from == null ? NULL : from.toString();
            }
        }
    };

    private static final Function<Annotation> ANNOTATION_LIST_FUNCTION = new Function<Annotation>() {

        @Override
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

    public static String formatInjectionPointType(Type type) {
        if (type instanceof Class<?>) {
            return ((Class<?>) type).getSimpleName();
        } else {
            return Formats.formatType(type);
        }
    }

    public static String formatType(Type baseType) {
        if (baseType == null) {
            return NULL;
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

            @Override
            public String apply(Type from, int position) {
                return commaDelimiterFunction().apply(formatType(from), position);
            }

        });
    }

    public static String formatBusinessInterfaceDescriptors(Iterable<? extends BusinessInterfaceDescriptor<?>> businessInterfaceDescriptors) {
        return formatIterable(businessInterfaceDescriptors, new Function<BusinessInterfaceDescriptor<?>>() {

            @Override
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

            @Override
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
        return wrapIfNecessary(formatIterable(actualTypeArguments, new Function<Type>() {

            @Override
            public String apply(Type from, int position) {
                return commaDelimiterFunction().apply(formatType(from), position);
            }

        }), "<", ">");
    }

    public static String wrapIfNecessary(String string, String prepend, String append) {
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
            return SNAPSHOT;
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
            return SNAPSHOT;
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
            if (qualifier.equals(SNAPSHOT) && timestamp != null) {
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

    /**
     * Attempts to extract a name of a missing class loader dependency from an exception such as {@link NoClassDefFoundError} or {@link ClassNotFoundException}.
     */
    public static String getNameOfMissingClassLoaderDependency(Throwable e) {
        if (e instanceof NoClassDefFoundError) {
            // NoClassDefFoundError sometimes includes CNFE as the cause. Since CNFE has a better formatted class name
            // and may also include classloader info, we prefer CNFE's over NCDFE's message.
            if (e.getCause() instanceof ClassNotFoundException) {
                return getNameOfMissingClassLoaderDependency(e.getCause());
            }
            if (e.getMessage() != null) {
                return e.getMessage().replace('/', '.');
            }
        }
        if (e instanceof ClassNotFoundException) {
            if (e.getMessage() != null) {
                return e.getMessage();
            }
        }
        if (e.getCause() != null) {
            return getNameOfMissingClassLoaderDependency(e.getCause());
        } else {
            return "[unknown]";
        }
    }
}
