/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.jsf;

import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;

import org.jboss.weld.module.jsf.JsfApiAbstraction;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.ApiAbstraction.Dummy;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Dan Allen
 */
public class JsfApiAbstractionTest {

    @Test
    public void testDetectsJsf12Version() {
        JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoaderHidingJsf20Classes());
        Assert.assertEquals(1.2, abstraction.MINIMUM_API_VERSION, 0);
        Assert.assertFalse(abstraction.isApiVersionCompatibleWith(2.0));
    }

    @Test
    public void testLoadsJsf12Classes() {
        JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoaderHidingJsf20Classes());
        Assert.assertEquals(FacesContext.class, abstraction.FACES_CONTEXT);
        Assert.assertEquals(Dummy.class, abstraction.BEHAVIOR_CLASS);
    }

    @Test
    public void testDetectsJsf20Version() {
        JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoader());
        Assert.assertEquals(2.0, abstraction.MINIMUM_API_VERSION, 0);
        Assert.assertTrue(abstraction.isApiVersionCompatibleWith(2.0));
    }

    @Test
    public void testLoadsJsf20Classes() {
        JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoader());
        Assert.assertEquals(FacesContext.class, abstraction.FACES_CONTEXT);
        Assert.assertEquals(Behavior.class, abstraction.BEHAVIOR_CLASS);
    }

    private ResourceLoader getResourceLoader() {
        return DefaultResourceLoader.INSTANCE;
    }

    private ResourceLoader getResourceLoaderHidingJsf20Classes() {
        return new DefaultResourceLoader() {

            @Override
            public Class<?> classForName(String name) {
                if ("javax.faces.component.behavior.Behavior".equals(name)) {
                    throw new ResourceLoadingException("Hidden class");
                }
                return super.classForName(name);
            }

        };
    }
}
