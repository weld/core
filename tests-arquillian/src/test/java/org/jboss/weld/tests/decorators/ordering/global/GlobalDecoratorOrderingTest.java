/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.ordering.global;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class GlobalDecoratorOrderingTest {

    @Inject
    private Decorated decorated;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(EnterpriseArchive.class, Utils.getDeploymentNameAsHash(GlobalDecoratorOrderingTest.class, Utils.ARCHIVE_TYPE.EAR)).addAsModule(getWebArchive()).addAsLibrary(getSharedLibrary());
    }

    public static Archive<?> getWebArchive() {
        JavaArchive thirdPartyLibrary = ShrinkWrap.create(JavaArchive.class).addClasses(ThirdPartyDecorator.class, ThirdPartyDecoratorExtension.class).addAsServiceProvider(Extension.class, ThirdPartyDecoratorExtension.class);

        BeansXml beans = new BeansXml(BeanDiscoveryMode.ALL);
        beans.decorators(LegacyDecorator1.class, LegacyDecorator2.class, LegacyDecorator3.class);
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addClasses(DecoratedImpl.class, LegacyDecorator1.class, LegacyDecorator2.class, LegacyDecorator3.class,
                        WebApplicationGlobalDecorator.class, GlobalDecoratorOrderingTest.class, DecoratorRegisteringExtension.class)
                .addAsWebInfResource(beans, "beans.xml").addAsServiceProvider(Extension.class, DecoratorRegisteringExtension.class)
                .addAsLibrary(thirdPartyLibrary);
    }

    public static Archive<?> getSharedLibrary() {
        return ShrinkWrap
                .create(JavaArchive.class)
                .addClasses(AbstractDecorator.class, Decorated.class, GloballyEnabledDecorator1.class,
                        GloballyEnabledDecorator2.class, GloballyEnabledDecorator3.class, GloballyEnabledDecorator4.class,
                        GloballyEnabledDecorator5.class, ExtensionEnabledDecorator1.class,
                        ExtensionEnabledDecorator2.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml");
    }

    @Test
    public void testDecoratorsInWebInfClasses() {
        List<String> expected = new ArrayList<String>();
        expected.add(ThirdPartyDecorator.class.getSimpleName());
        expected.add(GloballyEnabledDecorator1.class.getSimpleName());
        expected.add(ExtensionEnabledDecorator1.class.getSimpleName());
        expected.add(GloballyEnabledDecorator4.class.getSimpleName());
        expected.add(GloballyEnabledDecorator3.class.getSimpleName());
        expected.add(WebApplicationGlobalDecorator.class.getSimpleName());
        expected.add(GloballyEnabledDecorator2.class.getSimpleName());
        expected.add(ExtensionEnabledDecorator2.class.getSimpleName());
        expected.add(GloballyEnabledDecorator5.class.getSimpleName());
        expected.add(LegacyDecorator1.class.getSimpleName());
        expected.add(LegacyDecorator2.class.getSimpleName());
        expected.add(LegacyDecorator3.class.getSimpleName());
        expected.add(DecoratedImpl.class.getSimpleName());

        List<String> actual = new ArrayList<String>();
        decorated.getSequence(actual);
        assertEquals(expected, actual);
    }
}
