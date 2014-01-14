/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.util.reflection.Formats;

/**
 * Holds information about classes that have missing dependencies (class X references class Y and class Y is not
 * on classpath).
 *
 * @author Marko Luksa
 *
 */
public class MissingDependenciesRegistry extends AbstractBootstrapService {

    /**
     * Mapping between classes (their names) that are on the classpath and their missing dependencies.
     */
    private final ConcurrentMap<String, String> classToMissingClassMap = new ConcurrentHashMap<String, String>();

    public void registerClassWithMissingDependency(String className, String missingClassName) {
        classToMissingClassMap.put(className, missingClassName);
    }

    public void handleResourceLoadingException(String className, Throwable e) {
        String missingDependency = Formats.getNameOfMissingClassLoaderDependency(e);
        BootstrapLogger.LOG.ignoringClassDueToLoadingError(className, missingDependency);
        BootstrapLogger.LOG.catchingDebug(e);
        registerClassWithMissingDependency(className, missingDependency);
    }

    public String getMissingDependencyForClass(String className) {
        return classToMissingClassMap.get(className);
    }

    @Override
    public void cleanupAfterBoot() {
        classToMissingClassMap.clear();
    }
}
