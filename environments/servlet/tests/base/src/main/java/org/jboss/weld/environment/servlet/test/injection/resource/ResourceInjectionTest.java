/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.injection.resource;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ResourceInjectionTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment().addClasses(ResourceInjectionTest.class, Dolphin.class);
    }

    @Inject
    private Dolphin dolphin;

    @Test
    public void testFieldResourceInjection() {
        Assert.assertEquals("bar", dolphin.getFieldResource());
    }

    @Test
    public void testMethodResourceInjection() {
        Assert.assertEquals("bar", dolphin.getMethodResource());
    }
}
