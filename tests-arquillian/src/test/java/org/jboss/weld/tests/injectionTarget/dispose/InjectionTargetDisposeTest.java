/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.injectionTarget.dispose;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests fix for <a
 * href="https://issues.jboss.org/browse/WELD-2580">WELD-2580</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 *         target="_parent">Laird Nelson</a>
 */
@RunWith(Arquillian.class)
public class InjectionTargetDisposeTest {

    @Deployment
    public static final Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(InjectionTargetDisposeTest.class))
                .addPackage(InjectionTargetDisposeTest.class.getPackage())
                .addAsServiceProvider(Extension.class, DisposingExtension.class);
    }

    @Inject
    Instance<Widget> instanceWidget;

    @Test
    public void runOrdinaryContainerLifecycle() {
        Widget widget = instanceWidget.get();
        widget.ping();
        instanceWidget.destroy(widget);
        Assert.assertTrue(DisposingExtension.disposeCalled);
    }

}
