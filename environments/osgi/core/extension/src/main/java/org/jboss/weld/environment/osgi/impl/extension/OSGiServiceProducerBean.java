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
import org.jboss.weld.environment.osgi.impl.extension.beans.ServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This the bean class for all beans generated from a {@link Service} typed
 * {@link InjectionPoint}.
 * <b/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 *
 * @see OSGiServiceBean
 */
public class OSGiServiceProducerBean<Service> implements Bean<Service> {

    private static Logger logger =
                          LoggerFactory.getLogger(OSGiServiceProducerBean.class);

    private final InjectionPoint injectionPoint;

    private final BundleContext ctx;

    private Filter filter;

    private Set<Annotation> qualifiers;

    private Type type;

    public OSGiServiceProducerBean(InjectionPoint injectionPoint, BundleContext ctx) {
        logger.trace("Entering OSGiServiceProducerBean : "
                     + "OSGiServiceProducerBean() with parameter {}",
                     new Object[] {injectionPoint});
        this.injectionPoint = injectionPoint;
        this.ctx = ctx;
        type = injectionPoint.getType();
        qualifiers = injectionPoint.getQualifiers();
        filter = FilterGenerator.makeFilter(injectionPoint);
        logger.debug("New OSGiServiceProducerBean constructed {}", this);
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> s = new HashSet<Type>();
        s.add(type);
        s.add(Object.class);
        return s;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> result = new HashSet<Annotation>();
        result.addAll(qualifiers);
        result.add(new AnnotationLiteral<Any>() {
        });
        return result;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Class getBeanClass() {
        return (Class) ((ParameterizedType) type).getRawType();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Service create(CreationalContext creationalContext) {
        logger.trace("Entering OSGiServiceProducerBean : create() with parameter {}",
                     new Object[] {creationalContext});
        BundleContext registry = ctx;
        if (registry == null) {
            registry = FrameworkUtil.getBundle(
                injectionPoint.getMember().getDeclaringClass()).getBundleContext();
        }
        Service result =
                (Service) new ServiceImpl(((ParameterizedType) type).getActualTypeArguments()[0],
                                          registry,
                                          filter);
        logger.debug("New service for {} created {}", this, result);
        return result;
    }

    @Override
    public void destroy(Service instance,
                        CreationalContext<Service> creationalContext) {
        logger.trace("Entering OSGiServiceProducerBean : "
                     + "destroy() with parameter {} | {}",
                     new Object[] {instance, creationalContext});
        // Nothing to do, services are unget after each call.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OSGiServiceProducerBean)) {
            return false;
        }

        OSGiServiceProducerBean that = (OSGiServiceProducerBean) o;

        if (!filter.value().equals(that.filter.value())) {
            return false;
        }
        if (!getTypes().equals(that.getTypes())) {
            return false;
        }
        if (!getQualifiers().equals(that.getQualifiers())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getTypes().hashCode();
        result = 31 * result + filter.value().hashCode();
        result = 31 * result + getQualifiers().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OSGiServiceProducerBean ["
               + injectionPoint.getType().toString()
               + "] with qualifiers ["
               + printQualifiers()
               + "]";
    }

    public String printQualifiers() {
        String result = "";
        for (Annotation qualifier : getQualifiers()) {
            if (!result.equals("")) {
                result += " ";
            }
            result += "@" + qualifier.annotationType().getSimpleName();
            result += printValues(qualifier);
        }
        return result;
    }

    private String printValues(Annotation qualifier) {
        String result = "(";
        for (Method m : qualifier.annotationType().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Nonbinding.class)) {
                try {
                    Object value = m.invoke(qualifier);
                    if (value == null) {
                        value = m.getDefaultValue();
                    }
                    if (value != null) {
                        result += m.getName() + "=" + value.toString();
                    }
                }
                catch(Throwable t) {
                    // ignore
                }
            }
        }
        result += ")";
        return result.equals("()") ? "" : result;
    }

}
