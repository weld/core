/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.weld1779;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for https://issues.jboss.org/browse/WELD-1779
 *
 * @author Marcel Kolsteren
 *
 */
@RunWith(Arquillian.class)
public class ProducerProxyTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducerProxyTest.class)).addPackage(ProducerProxyTest.class.getPackage())
                .addAsServiceProvider(Extension.class, CustomScopeExtension.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("org.jboss.weld.enableUnsafeProxies"));
    }

    @Inject
    @Qualifier1
    private TestComponent requestScopedComponent;

    @Inject
    @Qualifier2
    private TestComponent customScopedComponent;

    @Inject
    private CustomScopeExtension customScopeExtension;

    @Test
    public void testCustomScopedComponent() {
        customScopeExtension.getContext().activate();
        customScopedComponent.setValue("test");
        customScopeExtension.getContext().deactivate();

        customScopeExtension.getContext().activate();
        Assert.assertNull(customScopedComponent.getValue());
        customScopeExtension.getContext().deactivate();
    }
}
