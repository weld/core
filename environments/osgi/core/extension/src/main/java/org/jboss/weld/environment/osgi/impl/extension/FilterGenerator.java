/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.impl.extension;

import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.annotation.Properties;
import org.jboss.weld.environment.osgi.api.annotation.Property;
import org.jboss.weld.environment.osgi.api.annotation.Required;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for generating {@link Filter} qualifier from various sources
 * (other {@link Filter}, {@link Qualifier}, {@link String} ...).
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class FilterGenerator {
    public static Filter makeFilter() {
        return new OSGiFilterQualifierType("");
    }

    public static Filter make(Set<String> tokens) {
        if (tokens == null || tokens.size() == 0) {
            return new OSGiFilterQualifierType("");
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (tokens.size() > 1) {
            stringBuilder.append("(&");
        }
        for (String token : tokens) {
            stringBuilder.append(token);
        }
        if (tokens.size() > 1) {
            stringBuilder.append(")");
        }
        return new OSGiFilterQualifierType(stringBuilder.toString());
    }

    public static Filter makeFilter(String filter) {
        return new OSGiFilterQualifierType(filter);
    }

    public static Filter makeFilter(Properties properties) {
        return make(tokenize(properties));
    }

    public static Filter makeFilter(Collection<Annotation> annotations) {
        Set<String> tokens = new HashSet<String>();
        tokens.addAll(tokenize(annotations));
        return make(tokens);
    }

    public static Filter makeFilter(InjectionPoint injectionPoint) {
        Set<Annotation> qualifiers = injectionPoint.getQualifiers();
        return FilterGenerator.makeFilter(qualifiers);
    }

    public static Filter makeFilter(Filter old, String filter) {
        Set<String> tokens = new HashSet<String>();
        if (old != null && old.value() != null && old.value().length() > 0) {
            tokens.add(old.value());
        }
        if (filter != null && filter.length() > 0) {
            tokens.add(filter);
        }
        return make(tokens);
    }

    public static Filter makeFilter(Filter old, Collection<Annotation> annotations) {
        Set<String> tokens = new HashSet<String>();
        if (old != null && old.value() != null && old.value().length() > 0) {
            tokens.add(old.value());
        }
        tokens.addAll(tokenize(annotations));
        return make(tokens);
    }

    private static Set<String> tokenize(Properties properties) {
        Set<String> result = new HashSet<String>();
        for (Property property : properties.value()) {
            result.add("(" + property.name().toLowerCase()
                    + "=" + property.value() + ")");
        }
        return result;
    }

    private static Set<String> tokenize(Collection<Annotation> annotations) {
        Set<String> result = new HashSet<String>();
        String current = "";
        if (!annotations.isEmpty()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                    if (annotation.annotationType().equals(Filter.class)) {
                        Filter old = (Filter) annotation;
                        if (old.value() != null && old.value().length() > 0) {
                            result.add(old.value());
                        }
                    } else if (annotation.annotationType().equals(Properties.class)) {
                        result.addAll(tokenize((Properties) annotation));
                    } else if (!annotation.annotationType().equals(Required.class)
                            && !annotation.annotationType().equals(OSGiService.class)
                            && !annotation.annotationType().equals(Default.class)
                            && !annotation.annotationType().equals(Any.class)) {
                        if (annotation.annotationType().getDeclaredMethods().length > 0) {
                            for (Method m : annotation.annotationType()
                                    .getDeclaredMethods()) {
                                if (!m.isAnnotationPresent(Nonbinding.class)) {
                                    try {
                                        Object value = m.invoke(annotation);
                                        if (value == null) {
                                            value = m.getDefaultValue();
                                            if (value == null) {
                                                value = "*";
                                            }
                                        }
                                        current = "(" + annotation.annotationType()
                                                .getSimpleName().toLowerCase()
                                                + "." + m.getName().toLowerCase()
                                                + "=" + value.toString() + ")";
                                        result.add(current);
                                    } catch (Throwable t) {// inaccessible property, skip
                                    }
                                }
                            }
                        } else {
                            current = "(" + annotation.annotationType().getSimpleName()
                                    .toLowerCase() + "=*)";
                            result.add(current);
                        }
                    }
                } else {
                    if (annotation.annotationType().isAnnotationPresent(Filter.class)) {
                        Filter old = annotation.annotationType()
                                .getAnnotation(Filter.class);
                        result.add(old.value());
                    }
                    if (annotation.annotationType()
                            .isAnnotationPresent(Properties.class)) {
                        Properties properties = annotation.annotationType()
                                .getAnnotation(Properties.class);
                        result.addAll(tokenize(properties));
                    }
                }
            }
        }
        return result;
    }

}
