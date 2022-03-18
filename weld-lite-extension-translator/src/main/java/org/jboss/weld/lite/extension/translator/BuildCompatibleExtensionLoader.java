/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SkipIfPortableExtensionPresent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A helper class used to load all implementations of {@link BuildCompatibleExtension} via service loader which also
 * allows to specify a {@link ClassLoader} for this process.
 *
 * <p>
 * Main use of this class is to give integrators the ability to pre-load all classes and determine if there are any such
 * extensions. If the returned set is empty, there is no need to register {@link LiteExtensionTranslator} portable extension
 * which may save some bootstrap time.
 * </p>
 *
 * @author Matej Novotny
 */
public class BuildCompatibleExtensionLoader {

    private static volatile List<Class<? extends BuildCompatibleExtension>> allKnownExtensions;

    // used as a lock in syncing access to the Collection
    private static final Object lock = new Object();

    private BuildCompatibleExtensionLoader() {
    }

    /**
     * Uses service classLoader to discover all {@link BuildCompatibleExtension} implementations. The parameter is a
     * {@code ClassLoader} that is to be used for this discovery. Returns a collection of all discovered extensions;
     * can be empty but can never return {@code null}.
     *
     * @param classLoader {@code ClassLoader} with which classes will be loaded
     * @return collection of build compatible extensions or an empty collection if none was found
     */
    public static List<Class<? extends BuildCompatibleExtension>> getBuildCompatibleExtensions(ClassLoader classLoader) {
        List<Class<? extends BuildCompatibleExtension>> listCopy = allKnownExtensions;
        if (listCopy == null) {
            synchronized (lock) {
                if (allKnownExtensions == null) {
                    allKnownExtensions = new ArrayList<>();

                    for (BuildCompatibleExtension extension : ServiceLoader.load(BuildCompatibleExtension.class, classLoader)) {
                        Class<? extends BuildCompatibleExtension> extensionClass = extension.getClass();
                        SkipIfPortableExtensionPresent skip = extensionClass.getAnnotation(SkipIfPortableExtensionPresent.class);
                        if (skip != null) {
                            continue;
                        }
                        allKnownExtensions.add(extensionClass);
                    }

                }
                return Collections.unmodifiableList(allKnownExtensions);
            }
        } else {
            return Collections.unmodifiableList(listCopy);
        }
    }

    /**
     * Uses service loader to discover all {@link BuildCompatibleExtension} implementations.
     * Returns a collection of all discovered extensions; can be empty but can never return {@code null}.
     * This method variant uses {@code Thread.currentThread().getContextClassLoader()} as the CL with which it
     * attempts to load all services.
     *
     * @return collection of build compatible extensions or an empty collection if none was found
     * @see {@link #getBuildCompatibleExtensions(ClassLoader)} for variant with explicit class loader
     */
    public static List<Class<? extends BuildCompatibleExtension>> getBuildCompatibleExtensions() {
        return getBuildCompatibleExtensions(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Clears out all discovered extensions. This is automatically performed from within {@link LiteExtensionTranslator}
     * once an instance of it is created.
     *
     * <p>
     * This is necessary because of testing environment where multiple deployments happen on the same running JVM and
     * the static list could be therefore retained in between deployments.
     * </p>
     */
    public static void clearDiscoveredExtensions() {
        synchronized (lock) {
            allKnownExtensions = null;
        }
    }
}
