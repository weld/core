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

package org.jboss.weld.osgi.tests.bundle1.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import org.jboss.weld.environment.osgi.api.annotation.BundleDataFile;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeader;
import org.jboss.weld.environment.osgi.api.annotation.BundleHeaders;
import org.jboss.weld.environment.osgi.api.annotation.Publish;

@Publish
public class BundleProvider {

    @Inject
    Bundle bundle;

    @Inject
    BundleContext bundleContext;

    @Inject @BundleHeaders
    Map<String,String> metadata;

    @Inject @BundleHeader("Bundle-SymbolicName")
    String symbolicName;

    @Inject @BundleDataFile("test.txt")
    File file;

    public Bundle getBundle() {
        return bundle;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public File getFile() {
        return file;
    }

}
