/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.AMPERSAND;
import static org.jboss.weld.probe.Strings.ARRAY;
import static org.jboss.weld.probe.Strings.CHEVRONS_LEFT;
import static org.jboss.weld.probe.Strings.CHEVRONS_RIGHT;
import static org.jboss.weld.probe.Strings.COMMA;
import static org.jboss.weld.probe.Strings.EMPTY;
import static org.jboss.weld.probe.Strings.EQUALS;
import static org.jboss.weld.probe.Strings.PARENTHESES_LEFT;
import static org.jboss.weld.probe.Strings.PARENTHESES_RIGHT;
import static org.jboss.weld.probe.Strings.QUTATION_MARK;
import static org.jboss.weld.probe.Strings.WILDCARD;
import static org.jboss.weld.probe.Strings.WILDCARD_EXTENDS;
import static org.jboss.weld.probe.Strings.WILDCARD_SUPER;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.Nonbinding;

import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.reflection.GenericArrayTypeImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.WildcardTypeImpl;

/**
 *
 * @author Martin Kouba
 */
@Vetoed
final class Parsers {

    private Parsers() {
    }

    /**
     * Type variables are not supported.
     *
     * @param value
     * @return the type
     */
    static Type parseType(String value, ResourceLoader resourceLoader) {
        value = value.trim();
        // Wildcards
        if (value.equals(WILDCARD)) {
            return WildcardTypeImpl.defaultInstance();
        }
        if (value.startsWith(WILDCARD_EXTENDS)) {
            Type upperBound = parseType(value.substring(WILDCARD_EXTENDS.length(), value.length()), resourceLoader);
            if (upperBound == null) {
                return null;
            }
            return WildcardTypeImpl.withUpperBound(upperBound);
        }
        if (value.startsWith(WILDCARD_SUPER)) {
            Type lowerBound = parseType(value.substring(WILDCARD_SUPER.length(), value.length()), resourceLoader);
            if (lowerBound == null) {
                return null;
            }
            return WildcardTypeImpl.withLowerBound(lowerBound);
        }
        // Array
        if (value.contains(ARRAY)) {
            Type componentType = parseType(value.substring(0, value.indexOf(ARRAY)), resourceLoader);
            if (componentType == null) {
                return null;
            }
            return new GenericArrayTypeImpl(componentType);
        }
        int chevLeft = value.indexOf(CHEVRONS_LEFT);
        String rawValue = chevLeft < 0 ? value : value.substring(0, chevLeft);
        Class<?> rawRequiredType = tryLoadClass(rawValue, resourceLoader);
        if (rawRequiredType == null) {
            return null;
        }
        if (rawRequiredType.getTypeParameters().length == 0) {
            return rawRequiredType;
        }
        // Parameterized type
        int chevRight = value.lastIndexOf(CHEVRONS_RIGHT);
        if (chevRight < 0) {
            return null;
        }
        List<String> parts = split(value.substring(chevLeft + 1, chevRight), ',', CHEVRONS_LEFT.charAt(0), CHEVRONS_RIGHT.charAt(0));
        Type[] typeParameters = new Type[parts.size()];
        for (int i = 0; i < typeParameters.length; i++) {
            Type typeParam = parseType(parts.get(i), resourceLoader);
            if (typeParam == null) {
                return null;
            }
            typeParameters[i] = typeParam;
        }
        return new ParameterizedTypeImpl(rawRequiredType, typeParameters);
    }

    /**
     *
     * @param qualifiers
     * @param beanManager
     * @return the list of qualifier instances
     */
    static List<QualifierInstance> parseQualifiers(String qualifiers, ResourceLoader resourceLoader, BeanManager beanManager) {
        List<QualifierInstance> qualifierInstances = new ArrayList<>();
        for (String qualifier : splitQualifiers(qualifiers)) {
            qualifierInstances.add(createQualifierInstance(qualifier, resourceLoader, beanManager));
        }
        return qualifierInstances;
    }

    /**
     *
     * @param qualifier
     * @param resourceLoader
     * @param beanManager
     * @return the qualifier instance or null
     */
    @SuppressWarnings("unchecked")
    static QualifierInstance createQualifierInstance(String qualifier, ResourceLoader resourceLoader, BeanManager beanManager) {
        String qualifierType = qualifier.contains("(") ? qualifier.substring(0, qualifier.indexOf("(")) : qualifier;
        Class<?> qualifierClass = tryLoadClass(qualifierType, resourceLoader);
        if (qualifierClass != null) {
            Class<? extends Annotation> qualifierAnnotationClass;
            try {
                qualifierAnnotationClass = (Class<? extends Annotation>) qualifierClass;
            } catch (Exception e) {
                return null;
            }
            if (beanManager.isQualifier(qualifierAnnotationClass)) {
                Map<String, Object> values;
                Method[] qualifierMembers = SecurityActions.getDeclaredMethods(qualifierAnnotationClass);
                if (qualifierMembers.length == 0) {
                    values = Collections.emptyMap();
                } else {
                    Map<String, String> memberValues = parseMemberValues(qualifier);
                    values = new HashMap<>();
                    for (Method method : qualifierMembers) {
                        if (!method.isAnnotationPresent(Nonbinding.class)) {
                            String value = memberValues.get(method.getName());
                            if (value == null) {
                                Object defaultValue = method.getDefaultValue();
                                if (defaultValue == null) {
                                    return null;
                                }
                                values.put(method.getName(), defaultValue);
                            } else {
                                Object extracted = extractValue(method, value);
                                if (extracted == null) {
                                    return null;
                                }
                                values.put(method.getName(), extracted);
                            }
                        }
                    }
                }
                return new QualifierInstance(qualifierAnnotationClass, Collections.unmodifiableMap(values));
            }
        }
        return null;
    }

    private static List<String> splitQualifiers(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return split(value.replace(AMPERSAND, EMPTY), COMMA.charAt(0), PARENTHESES_LEFT.charAt(0), PARENTHESES_RIGHT.charAt(0));
    }

    private static Map<String, String> parseMemberValues(String qualifier) {
        if (qualifier.contains(PARENTHESES_LEFT) && qualifier.contains(PARENTHESES_RIGHT)) {
            Map<String, String> values = new HashMap<>();
            List<String> parts = split(qualifier.substring(qualifier.indexOf(PARENTHESES_LEFT) + 1, qualifier.lastIndexOf(PARENTHESES_RIGHT)), COMMA.charAt(0),
                    QUTATION_MARK.charAt(0), QUTATION_MARK.charAt(0));
            for (String part : parts) {
                values.put(part.substring(0, part.indexOf(EQUALS)), part.substring(part.indexOf(EQUALS) + 1, part.length()));
            }
            return values;
        }
        return Collections.emptyMap();
    }

    private static List<String> split(String value, char on, char ignoreStart, char ignoreEnd) {
        List<String> values = new ArrayList<>();
        boolean ignore = false;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == on) {
                if (!ignore) {
                    if (buffer.length() > 0) {
                        values.add(buffer.toString());
                        buffer = new StringBuilder();
                    }
                } else {
                    buffer.append(value.charAt(i));
                }
            } else {
                if (value.charAt(i) == ignoreStart) {
                    ignore = true;
                } else if (value.charAt(i) == ignoreEnd) {
                    ignore = false;
                }
                buffer.append(value.charAt(i));
            }
        }
        if (buffer.length() > 0) {
            values.add(buffer.toString());
        }
        return values;
    }

    private static Object extractValue(Method member, String value) {
        Class<?> type = member.getReturnType();
        if (type.equals(String.class) && value.startsWith(QUTATION_MARK) && value.endsWith(QUTATION_MARK)) {
            return value.substring(1, value.length() - 1);
        } else if (type.isPrimitive()) {
            if (Boolean.TYPE.equals(type)) {
                return Boolean.valueOf(value);
            } else if (Character.TYPE.equals(type)) {
                return Character.valueOf(value.charAt(0));
            } else if (Byte.TYPE.equals(type)) {
                return Byte.valueOf(value);
            } else if (Short.TYPE.equals(type)) {
                return Short.valueOf(value);
            } else if (Integer.TYPE.equals(type)) {
                return Integer.valueOf(value);
            } else if (Long.TYPE.equals(type)) {
                return Long.valueOf(value);
            } else if (Float.TYPE.equals(type)) {
                return Float.valueOf(value);
            } else if (Double.TYPE.equals(type)) {
                return Double.valueOf(value);
            }
        } else if (type.equals(Class.class)) {
            return Reflections.loadClass(value, WeldClassLoaderResourceLoader.INSTANCE);
        }
        // We do not support annotation-valued and array-valued members
        // These should be annotated @Nonbinding in a portable application (see also "5.2.6. Qualifier annotations with members")
        return null;
    }

    private static Class<?> tryLoadClass(String value, ResourceLoader resourceLoader) {
        Class<?> result = null;
        if (resourceLoader != null) {
            // First use the provided ResourceLoader - most probably an implementation that is aware of the module classloader
            result = Reflections.loadClass(value, resourceLoader);
        }
        return result == null ? Reflections.loadClass(value, WeldClassLoaderResourceLoader.INSTANCE) : result;
    }
}
