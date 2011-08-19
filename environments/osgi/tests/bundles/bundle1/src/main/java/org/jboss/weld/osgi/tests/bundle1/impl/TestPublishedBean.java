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

package org.jboss.weld.osgi.tests.bundle1.impl;

import org.jboss.weld.osgi.tests.bundle1.api.Name2;
import org.jboss.weld.osgi.tests.bundle1.api.PropertyService;
import org.jboss.weld.osgi.tests.bundle1.api.TestPublished;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.annotation.Publish;

@Publish
@ApplicationScoped
public class TestPublishedBean implements TestPublished {

    @Inject @OSGiService @Filter("(Name.value=2)") PropertyService  service;

    @Inject @OSGiService @Name2 PropertyService  service2;

    public PropertyService getService() {
        return service;
    }

    public PropertyService getService2() {
        return service2;
    }
}
