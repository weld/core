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

import org.jboss.weld.environment.osgi.impl.annotation.OSGiServiceAnnotation;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an {@link AnnotatedField} that wrap all {@link OSGiService}
 * annotated parameter processed by Weld-OSGi bean bundles Weld containers,
 * to avoid ambiguous dependency with regular CDI injection point.
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 *
 * @see OSGiServiceAnnotatedParameter
 * @see OSGiServiceAnnotatedType
 */
public class OSGiServiceAnnotatedField<T> implements AnnotatedField<T> {

    private static Logger logger =
                          LoggerFactory.getLogger(OSGiServiceAnnotatedField.class);

    private AnnotatedField field;

    private Set<Annotation> annotations = new HashSet<Annotation>();

    private Filter filter;

    public OSGiServiceAnnotatedField(final AnnotatedField<? super T> field) {
        logger.debug("Creation of a new CDIOSGiAnnotatedField wrapping {}", field);
        this.field = field;
        filter = FilterGenerator.makeFilter(filter, field.getAnnotations());
        annotations.add(filter);
        //annotations.add(new AnnotationLiteral<OSGiService>() {});
        annotations.add(new OSGiServiceAnnotation(
                field.getJavaMember().getAnnotation(OSGiService.class).value()));
        if (field.getAnnotation(Required.class) != null) {
            annotations.add(new AnnotationLiteral<Required>() {
            });
        }
        for (Annotation annotation : field.getAnnotations()) {
            if (!annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                annotations.add(annotation);
            }
        }
        logger.debug("New OSGiServiceAnnotatedField constructed {}", this);
    }

    @Override
    public Field getJavaMember() {
        return field.getJavaMember();
    }

    @Override
    public boolean isStatic() {
        return field.isStatic();
    }

    @Override
    public AnnotatedType<T> getDeclaringType() {
        return field.getDeclaringType();
    }

    @Override
    public Type getBaseType() {
        return field.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return field.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationType)) {
                return (T) annotation;
            }
        }
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (getAnnotation(annotationType) == null) {
            return false;
        }
        return true;
    }

}
