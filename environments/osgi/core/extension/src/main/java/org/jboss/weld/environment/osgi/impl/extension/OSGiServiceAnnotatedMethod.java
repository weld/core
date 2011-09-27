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

import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is an {@link AnnotatedMethod} that wrap all {@link Inject}
 * annotated method processed by Weld-OSGi bean bundles Weld containers.
 * <p/>
 * It wrap any {@link OSGiService} annotated injection point in these
 * methods to avoid ambiguous dependency with regular CDI injection point.
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 *
 * @see OSGiServiceAnnotatedParameter
 * @see OSGiServiceAnnotatedType
 */
public class OSGiServiceAnnotatedMethod<T> implements AnnotatedMethod<T> {

    private static Logger logger =
                          LoggerFactory.getLogger(OSGiServiceAnnotatedMethod.class);

    AnnotatedMethod method;

    List<AnnotatedParameter<T>> parameters =
                                new ArrayList<AnnotatedParameter<T>>();

    public OSGiServiceAnnotatedMethod(AnnotatedMethod<? super T> method) {
        logger.trace("Entering OSGiServiceAnnotatedMethod : "
                     + "OSGiServiceAnnotatedMethod() with parameter {}",
                     new Object[] {method});
        this.method = method;
        for (AnnotatedParameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(OSGiService.class)) {
                parameters.add(new OSGiServiceAnnotatedParameter(parameter));
            }
            else {
                parameters.add(parameter);
            }
        }
        logger.debug("New OSGiServiceAnnotatedMethod constructed {}", this);
    }

    @Override
    public Method getJavaMember() {
        return method.getJavaMember();
    }

    @Override
    public boolean isStatic() {
        return method.isStatic();
    }

    @Override
    public AnnotatedType<T> getDeclaringType() {
        return method.getDeclaringType();
    }

    @Override
    public List<AnnotatedParameter<T>> getParameters() {
        return parameters;
    }

    @Override
    public Type getBaseType() {
        return method.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return method.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return method.getAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return method.isAnnotationPresent(annotationType);
    }

}
