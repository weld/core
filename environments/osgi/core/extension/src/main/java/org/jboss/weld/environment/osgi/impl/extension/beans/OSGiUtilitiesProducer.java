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
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jboss.weld.environment.osgi.api.BundleState;
import org.jboss.weld.environment.osgi.api.Registration;
import org.jboss.weld.environment.osgi.api.RegistrationHolder;
import org.jboss.weld.environment.osgi.api.annotation.BundleDataFile;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeader;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeaders;
import org.jboss.weld.environment.osgi.api.annotation.BundleName;
import org.jboss.weld.environment.osgi.api.annotation.BundleVersion;

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
    public BundleState getBundleState(BundleHolder holder) {
        logger.trace("Entering {} : {} with parameter {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder
                });
        logger.debug("Returning the current bundle {} bundle state {}",
                     holder.getBundle(),
                     holder.getState());
        return holder.getState();
    }

    @Produces
    public Bundle getBundle(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
        logger.debug("Returning the current bundle {}", holder.getBundle());
        return holder.getBundle();
    }

    @Produces
    @BundleName("")
    @BundleVersion("")
    public Bundle getSpecificBundle(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
        Set<Annotation> qualifiers = p.getQualifiers();
        BundleName bundleName = null;
        BundleVersion bundleVersion = null;
        for (Annotation qualifier : qualifiers) {
            if (qualifier.annotationType().equals(BundleName.class)) {
                bundleName = (BundleName) qualifier;
            }
            else if (qualifier.annotationType().equals(BundleVersion.class)) {
                bundleVersion = (BundleVersion) qualifier;
            }
        }
        if (bundleName == null || bundleName.value().equals("")) {
            logger.debug("Returning the current bundle {}", holder.getBundle());
            return holder.getBundle();
        }
        else {
            if (bundleVersion == null || bundleVersion.value().equals("")) {
                Bundle result =
                       (Bundle) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                       new Class[] { Bundle.class },
                                                       new BundleHandler(
                        bundleName.value(),
                        "",
                        holder.getContext()));
                logger.debug("Returning the bundle {}:no_version_provided",
                             bundleName.value());
                return result;
            }
            Bundle result =
                   (Bundle) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                   new Class[]{
                        Bundle.class
                    },
                                                   new BundleHandler(
                    bundleName.value(),
                    bundleVersion.value(),
                    holder.getContext()));
            logger.debug("Returning the bundle {}:{}",
                         bundleName.value(),
                         bundleVersion.value());
            return result;
        }
    }

    @Produces
    public BundleContext getBundleContext(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
        logger.debug("Returning the current bundle {} bundle context {}",
                     holder.getBundle(),
                     holder.getContext());
        return holder.getContext();
    }

    @Produces
    @BundleName("")
    @BundleVersion("")
    public BundleContext getSpecificContext(BundleHolder holder,
                                            InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
        BundleContext result =
                      (BundleContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                             new Class[]{
                    BundleContext.class
                },
                                                             new BundleContextHandler(getSpecificBundle(holder, p)));
        logger.debug("Returning the proxy for bundle context {}", result);
        return result;
    }

    @Produces
    @BundleDataFile("")
    public File getDataFile(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
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
        BundleContext context = getSpecificContext(holder, p);
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
        logger.trace("Entering {} : {} with parameters {} | {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    bundleHolder,
                    holder,
                    p
                });
        Class<T> contract = ((Class<T>) ((ParameterizedType) p.getType()).getActualTypeArguments()[0]);
        Registration<T> result = new RegistrationImpl<T>(contract,
                                                         bundleHolder.getContext(),
                                                         bundleHolder.getBundle(),
                                                         holder);
        logger.debug("Returning the registrations {}", result);
        return result;
    }

    @Produces
    @BundleName("")
    @BundleVersion("")
    @BundleHeaders
    public Map<String, String> getBundleHeaders(BundleHolder holder,
                                                InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
        Dictionary dict = getSpecificBundle(holder, p).getHeaders();
        if (dict == null) {
            logger.warn("Returning null, unable to retrieve the dictionary headers"
                        + " for bundle {}", getSpecificBundle(holder, p));
            return null;
        }
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> keys = dict.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            headers.put(key, (String) dict.get(key));
        }
        logger.debug("Returning the headers {} "
                     + "for bundle {} getSpecificBundle(holder, p)",
                     headers,
                     getSpecificBundle(holder, p));
        return headers;
    }

    @Produces
    @BundleName("")
    @BundleVersion("")
    @BundleHeader("")
    public String getSpecificBundleHeader(BundleHolder holder, InjectionPoint p) {
        logger.trace("Entering {} : {} with parameters {} | {}",
                     new Object[]{
                    getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    holder,
                    p
                });
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
        Dictionary dict = getSpecificBundle(holder, p).getHeaders();
        if (dict == null) {
            logger.warn("Returning null, unable to retrieve the dictionary headers"
                        + " for bundle {}", getSpecificBundle(holder, p));
            return null;
        }
        logger.debug("Returning the header {} value "
                     + "for bundle {} getSpecificBundle(holder, p)",
                     header,
                     getSpecificBundle(holder, p));
        return (String) dict.get(header.value());
    }

    private static class BundleHandler implements InvocationHandler {
        private final String symbolicName;

        private final Version version;

        private final BundleContext context;

        public BundleHandler(String symbolicName,
                             String version,
                             BundleContext context) {
            this.symbolicName = symbolicName;
            this.context = context;
            if (!version.equals("")) {
                this.version = new Version(version);
            }
            else {
                this.version = null;
            }
            logger.trace("Bundle handler for bundle {}:{} created",
                         symbolicName,
                         version.equals("") ? "no_version_provided" : version);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            logger.trace("{} intercepting call "
                         + "to method {} with args {} for bundle {}:{}",
                         new Object[]{
                        getClass().getName(),
                        method,
                        args,
                        symbolicName,
                        version == null ? "no_version_provided" : version
                    });
            Bundle bundle = null;
            Bundle[] bundles = context.getBundles();
            if (bundles != null) {
                for (Bundle b : bundles) {
                    if (b.getSymbolicName().equals(symbolicName)) {
                        if (version != null) {
                            if (version.equals(b.getVersion())) {
                                bundle = b;
                                break;
                            }
                        }
                        else {
                            bundle = b;
                            break;
                        }
                    }
                }
            }
            if (bundle == null) {
                logger.warn("Returning null, bundle {}:{} is unavailable",
                            symbolicName,
                            version == null ? "no_version_provided" : version);
                return null;
            }
            logger.debug("Calling method {} with args {} on bundle {}:{}",
                         new Object[]{
                        method,
                        args,
                        symbolicName,
                        version == null ? "no_version_provided" : version
                    });
            return method.invoke(bundle, args);
        }

    }

    private static class BundleContextHandler implements InvocationHandler {
        Bundle bundle;

        private BundleContextHandler(Bundle bundle) {
            this.bundle = bundle;
            logger.trace("Bundle context handler for bundle {} created",
                         bundle);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            logger.trace("{} intercepting call "
                         + "to method {} with args {} for bundle {} bundle context",
                         new Object[]{
                        getClass().getName(),
                        method,
                        args,
                        bundle
                    });
            int state = 0;
            try {
                state = bundle.getState();
            }
            catch(Exception e) {
                logger.warn("Returning null, unable to retrieve bundle {}",
                            bundle);
                return null;
            }
            if (state != Bundle.ACTIVE
                && state != Bundle.STARTING
                && state != Bundle.STOPPING) {
                logger.warn("Returning null, "
                            + "the bundle {} was not active, starting or stopping",
                            bundle);
                return null;
            }
            BundleContext context = bundle.getBundleContext();
            if (context == null) {
                logger.warn("Returning null, "
                            + "unable to retrieve bundle context for bundle {}",
                            bundle);
                return null;
            }
            logger.debug("Calling method {} with args {} "
                         + "on bundle context {}",
                         new Object[]{
                        method,
                        args,
                        context
                    });
            return method.invoke(context, args);
        }

    }
}