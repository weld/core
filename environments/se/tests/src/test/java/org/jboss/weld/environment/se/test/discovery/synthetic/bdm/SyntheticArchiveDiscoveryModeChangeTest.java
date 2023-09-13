/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.discovery.synthetic.bdm;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.discovery.synthetic.bdm.discoveredPackage.Foo;
import org.jboss.weld.environment.se.test.discovery.synthetic.bdm.hiddenPackage.Bar;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class SyntheticArchiveDiscoveryModeChangeTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).annotated().addPackages(true,
                SyntheticArchiveDiscoveryModeChangeTest.class.getPackage());
    }

    @Test
    public void testDiscoveryModeAnnotated() {
        Weld weld = new Weld();
        weld.disableDiscovery().setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED).addPackages(Foo.class.getPackage(),
                Bar.class.getPackage());
        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertFalse(container.select(Bar.class).isResolvable());
        }
    }

    @Test
    public void testDiscoveryModeAll() {
        Weld weld = new Weld();
        // this is same as default
        weld.disableDiscovery().setBeanDiscoveryMode(BeanDiscoveryMode.ALL).addPackages(Foo.class.getPackage(),
                Bar.class.getPackage());
        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        }
    }

    @Test
    public void testDiscoveryModeNone() {
        Weld weld = new Weld();
        weld.disableDiscovery().addPackages(Foo.class.getPackage(), Bar.class.getPackage());
        try {
            weld.setBeanDiscoveryMode(BeanDiscoveryMode.NONE);
        } catch (IllegalArgumentException e) {
            // expected, end test
            return;
        }
        Assert.fail();
    }

}
