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
package org.jboss.weld.environment.osgi.impl.extension.beans;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jboss.weld.environment.osgi.api.Registration;
import org.jboss.weld.environment.osgi.api.RegistrationHolder;
import org.jboss.weld.environment.osgi.api.annotation.BundleDataFile;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeader;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeaders;

/**
 * This the class responsible for OSGi utility injection for the current bundle.
 * <b/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class OSGiUtilitiesProducer {

    private static Logger logger =
                          LoggerFactory.getLogger(OSGiUtilitiesProducer.class);

    @Produces
    public Bundle getBundle(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering OSGiUtilitiesProducer : getBundle() "
                     + "with parameters {} | {}",
                     new Object[] {holder,p});
        logger.debug("Returning the current bundle {}", holder.getBundle());
        return holder.getBundle();
    }

    @Produces
    public BundleContext getBundleContext(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering OSGiUtilitiesProducer : getBundleContext() "
                + "with parameters {} | {}",
                     new Object[] {holder,p});
        logger.debug("Returning the current bundle {} bundle context {}",
                     holder.getBundle(),
                     holder.getContext());
        return holder.getContext();
    }

    @Produces
    @BundleDataFile("")
    public File getDataFile(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering OSGiUtilitiesProducer : getDataFile() "
                + "with parameters {} | {}",
                     new Object[] {holder,p});
        Set<Annotation> qualifiers = p.getQualifiers();
        BundleDataFile file = null;
        for (Annotation qualifier : qualifiers) {
            if (qualifier.annotationType().equals(BundleDataFile.class)) {
                file = (BundleDataFile) qualifier;
                break;
            }
        }
        if (file.value().equals("")) {
            logger.warn("Returning null,"
                        + " the BundleDataFile annotation path was empty");
            return null;
        }
        BundleContext context = getBundleContext(holder, p);
        if (context == null) {
            logger.warn("Returning null, unable to retrieve the BundleContext "
                        + "for holder {} and injection point {}", holder, p);
            return null;
        }
        logger.debug("Returning the file {} from bundle context {}",
                     file.value(),
                     context);
        return context.getDataFile(file.value());
    }

    @Produces
    public <T> Registration<T> getRegistrations(BundleHolder bundleHolder,
                                                RegistrationHolder holder,
                                                InjectionPoint p) {
        logger.trace("Entering OSGiUtilitiesProducer : getRegistrations() "
                + "with parameters {} | {} | {}",
                     new Object[] {bundleHolder,holder,p});
        Class<T> contract = ((Class<T>) ((ParameterizedType) p.getType()).getActualTypeArguments()[0]);
        Registration<T> result = new RegistrationImpl<T>(contract,
                                                         bundleHolder.getContext(),
                                                         bundleHolder.getBundle(),
                                                         holder);
        logger.debug("Returning the registrations {}", result);
        return result;
    }

    @Produces
    @BundleHeaders
    public Map<String, String> getBundleHeaders(BundleHolder holder,
                                                InjectionPoint p) {
        logger.trace("Entering OSGiUtilitiesProducer : getBundleHeaders() "
                + "with parameters {} | {}",
                     new Object[] {holder,p});
        Dictionary dict = getBundle(holder, p).getHeaders();
        if (dict == null) {
            logger.warn("Returning null, unable to retrieve the dictionary headers"
                        + " for bundle {}", getBundle(holder, p));
            return null;
        }
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> keys = dict.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            headers.put(key, (String) dict.get(key));
        }
        logger.debug("Returning the headers {} "
                     + "for bundle {}",
                     headers,
                     getBundle(holder, p));
        return headers;
    }

    @Produces
    @BundleHeader("")
    public String getBundleHeader(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering OSGiUtilitiesProducer : getBundleHeader() "
                + "with parameters {} | {}",
                     new Object[] {holder,p});
        Set<Annotation> qualifiers = p.getQualifiers();
        BundleHeader header = null;
        for (Annotation qualifier : qualifiers) {
            if (qualifier.annotationType().equals(BundleHeader.class)) {
                header = (BundleHeader) qualifier;
                break;
            }
        }
        if (header == null || header.value().equals("")) {
            logger.warn("Returning null, "
                        + "the BundleHeader annotation name was empty");
            return null;
        }
        Dictionary dict = getBundle(holder, p).getHeaders();
        if (dict == null) {
            logger.warn("Returning null, unable to retrieve the dictionary headers"
                        + " for bundle {}", getBundle(holder, p));
            return null;
        }
        logger.debug("Returning the header {} value "
                     + "for bundle {}",
                     header,
                     getBundle(holder, p));
        return (String) dict.get(header.value());
    }
}