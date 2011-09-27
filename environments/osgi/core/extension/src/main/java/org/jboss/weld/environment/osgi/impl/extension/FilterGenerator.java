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
import org.jboss.weld.environment.osgi.impl.annotation.FilterAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for generating {@link Filter} qualifier from various sources
 * (other {@link Filter}, {@link Qualifier}, {@link String} ...).
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class FilterGenerator {

    private static Logger logger =
                          LoggerFactory.getLogger(FilterGenerator.class);

    /**
     * Produce an empty {@link Filter} annotation.
     * <p/>
     * @return an empty {@link Filter} annotation.
     *
     * @see FilterAnnotation
     */
    public static Filter makeFilter() {
        logger.trace("Entering FilterGenerator : "
                     + "makeFilter() with no parameter");
        return new FilterAnnotation("");
    }

    /**
     * Produce a {@link Filter} annotation from a set of string. Each string
     * may be a valid LDAP filter token: (name=value).
     * <p/>
     * @param tokens the set of LDAP filter to use.
     * @return a {@link Filter} annotation as (&token1token2..tokeni) or an
     * empty {@link Filter} annotation if tokens was empty or null.
     * @see FilterAnnotation
     */
    public static Filter make(Set<String> tokens) {
        logger.trace("Entering FilterGenerator : make() with parameter {}",
                     new Object[] {tokens});
        if (tokens == null || tokens.isEmpty()) {
            return new FilterAnnotation("");
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
        return new FilterAnnotation(stringBuilder.toString());
    }

    /**
     * Produce a {@link Filter} annotation from a string. This string
     * may be a valid LDAP filter.
     * <p/>
     * @param filter the LDAP filter to use.
     * @return a {@link Filter} annotation
     * @see FilterAnnotation
     */
    public static Filter makeFilter(String filter) {
        logger.trace("Entering FilterGenerator : "
                     + "makeFilter() with parameter {}",
                     new Object[] {filter});
        return new FilterAnnotation(filter);
    }

    /**
     * Produce a {@link Filter} annotation from a {@link Properties}. Each
     * {@link Property} may be a valid LDAP filter name/value pair.
     * <p/>
     * @param properties the properties to use.
     * @return a {@link Filter} annotation from tokenized properties.
     * @see FilterAnnotation
     */
    public static Filter makeFilter(Properties properties) {
        return make(tokenize(properties));
    }

    /**
     * Produce a {@link Filter} annotation from a collection of annotation. Each
     * {@link Annotation} may be a valid {@link Qualifier}.
     * <p/>
     * @param annotations the qualifiers to use.
     * @return a {@link Filter} annotation from tokenized annotations.
     * @see FilterAnnotation
     */
    public static Filter makeFilter(Collection<Annotation> annotations) {
        logger.trace("Entering FilterGenerator : "
                     + "makeFilter() with parameter {}",
                     new Object[] {annotations});
        return make(tokenize(annotations));
    }

    /**
     * Produce a {@link Filter} annotation from an injection point annotations.
     * <p/>
     * @param injectionPoint the injection point to use.
     * @return a {@link Filter} annotation from injection point annotations.
     * @see FilterAnnotation
     * @see FilterGenerator#makeFilter(java.util.Collection)
     */
    public static Filter makeFilter(InjectionPoint injectionPoint) {
        logger.trace("Entering FilterGenerator : "
                     + "makeFilter() with parameter {}",
                     new Object[] {injectionPoint});
        Set<Annotation> qualifiers = injectionPoint.getQualifiers();
        return FilterGenerator.makeFilter(qualifiers);
    }

    /**
     * Produce a {@link Filter} annotation from another filter and a string.
     * This string may be a valid LDAP filter.
     * <p/>
     * @param old the old filter to update.
     * @param filter the LDAP filter to use.
     * @return a {@link Filter} annotation from old filter and string.
     * @see FilterAnnotation
     */
    public static Filter makeFilter(Filter old, String filter) {
        logger.trace("Entering FilterGenerator : "
                     + "makeFilter() with parameters {} | {}",
                     new Object[] {old,
                                   filter});
        Set<String> tokens = new HashSet<String>();
        if (old != null && old.value() != null && old.value().length() > 0) {
            tokens.add(old.value());
        }
        if (filter != null && filter.length() > 0) {
            tokens.add(filter);
        }
        return make(tokens);
    }

    /**
     * Produce a {@link Filter} annotation from another filter and a
     * {@link Properties}. Each {@link Property} may be a valid LDAP filter
     * name/value pair.
     * <p/>
     * @param old the old filter to update.
     * @param annotations the qualifiers to use.
     * @return a {@link Filter} annotation from old filter and tokenized
     * annotations.
     * @see FilterAnnotation
     */
    public static Filter makeFilter(Filter old,
                                    Collection<Annotation> annotations) {
        logger.trace("Entering FilterGenerator : "
                     + "makeFilter() with parameters {} | {}",
                     new Object[] {old,
                                   annotations});
        Set<String> tokens = new HashSet<String>();
        if (old != null && old.value() != null && old.value().length() > 0) {
            tokens.add(old.value());
        }
        tokens.addAll(tokenize(annotations));
        return make(tokens);
    }

    private static Set<String> tokenize(Properties properties) {
        logger.trace("Entering FilterGenerator : "
                     + "tokenize() with parameter {}",
                     new Object[] {properties});
        Set<String> result = new HashSet<String>();
        for (Property property : properties.value()) {
            result.add("(" + property.name().toLowerCase()
                       + "=" + property.value() + ")");
        }
        return result;
    }

    private static Set<String> tokenize(Collection<Annotation> annotations) {
        logger.trace("Entering FilterGenerator : "
                     + "tokenize() with parameter {}",
                     new Object[] {annotations});
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
                    }
                    else if (annotation.annotationType().equals(Properties.class)) {
                        result.addAll(tokenize((Properties) annotation));
                    }
                    else if (!annotation.annotationType().equals(Required.class)
                             && !annotation.annotationType().equals(OSGiService.class)
                             && !annotation.annotationType().equals(Default.class)
                             && !annotation.annotationType().equals(Any.class)) {
                        if (annotation.annotationType().getDeclaredMethods().length > 0) {
                            for (Method m : annotation.annotationType().getDeclaredMethods()) {
                                if (!m.isAnnotationPresent(Nonbinding.class)) {
                                    try {
                                        Object value = m.invoke(annotation);
                                        if (value == null) {
                                            value = m.getDefaultValue();
                                            if (value == null) {
                                                value = "*";
                                            }
                                        }
                                        current = "(" + annotation.annotationType().getSimpleName().toLowerCase()
                                                  + "." + m.getName().toLowerCase()
                                                  + "=" + value.toString() + ")";
                                        result.add(current);
                                    }
                                    catch(Throwable t) {// inaccessible property, skip
                                    }
                                }
                            }
                        }
                        else {
                            current = "(" + annotation.annotationType().getSimpleName().toLowerCase() + "=*)";
                            result.add(current);
                        }
                    }
                }
                else {
                    if (annotation.annotationType().isAnnotationPresent(Filter.class)) {
                        Filter old = annotation.annotationType().getAnnotation(Filter.class);
                        result.add(old.value());
                    }
                    if (annotation.annotationType().isAnnotationPresent(Properties.class)) {
                        Properties properties = annotation.annotationType().getAnnotation(Properties.class);
                        result.addAll(tokenize(properties));
                    }
                }
            }
        }
        return result;
    }

}
