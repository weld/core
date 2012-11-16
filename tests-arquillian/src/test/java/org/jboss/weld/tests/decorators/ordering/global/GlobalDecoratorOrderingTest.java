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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeansXmlClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
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
        return ShrinkWrap.create(EnterpriseArchive.class).addAsModule(getWebArchive()).addAsLibrary(getSharedLibrary());
    }

    public static Archive<?> getWebArchive() {
        BeansXml beans = new BeansXml();
        // legacy decorators - decorators enabled for this BDA only
        beans.decorators(new BeansXmlClass(LegacyDecorator1.class));
        beans.decorators(new BeansXmlClass(LegacyDecorator2.class));
        beans.decorators(new BeansXmlClass(LegacyDecorator3.class));
        // locally disable GED3
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator3.class, false));
        // locally enable GPD1
        beans.decorators(new BeansXmlClass(GloballyPrioritizedDecorator1.class, true));
        // locally override GED5 to behave as a legacy enablement
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator5.class));
        // globally enable WAGD
        beans.decorators(new BeansXmlClass(WebApplicationGlobalDecorator.class, 1008));
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addClasses(DecoratedImpl.class, LegacyDecorator1.class, LegacyDecorator2.class, LegacyDecorator3.class,
                        WebApplicationGlobalDecorator.class, GlobalDecoratorOrderingTest.class)
                .addAsWebInfResource(beans, "beans.xml");
    }

    public static Archive<?> getSharedLibrary() {
        BeansXml beans = new BeansXml();
        // globally enabled decorators
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator4.class, 1025));
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator5.class, 800));
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator1.class, 995));
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator2.class, 1005));
        beans.decorators(new BeansXmlClass(GloballyEnabledDecorator3.class, 1015));
        // decorators with globally set priority (but disabled)
        beans.decorators(new BeansXmlClass(GloballyPrioritizedDecorator1.class, false, 1015));
        beans.decorators(new BeansXmlClass(GloballyPrioritizedDecorator2.class, false, 1020));

        return ShrinkWrap
                .create(JavaArchive.class)
                .addClasses(AbstractDecorator.class, Decorated.class, GloballyEnabledDecorator1.class,
                        GloballyEnabledDecorator2.class, GloballyEnabledDecorator3.class, GloballyEnabledDecorator4.class,
                        GloballyEnabledDecorator5.class, GloballyPrioritizedDecorator1.class,
                        GloballyPrioritizedDecorator2.class).addAsManifestResource(beans, "beans.xml");
    }

    @Test
    public void testDecoratorsInWebInfClasses() {
        List<String> expected = new ArrayList<String>();
        expected.add(GloballyEnabledDecorator1.class.getSimpleName());
        expected.add(LegacyDecorator1.class.getSimpleName());
        expected.add(GloballyEnabledDecorator2.class.getSimpleName());
        expected.add(LegacyDecorator2.class.getSimpleName());
        expected.add(GloballyPrioritizedDecorator1.class.getSimpleName());
        expected.add(LegacyDecorator3.class.getSimpleName());
        expected.add(GloballyEnabledDecorator4.class.getSimpleName());
        expected.add(GloballyEnabledDecorator5.class.getSimpleName());
        expected.add(DecoratedImpl.class.getSimpleName());

        List<String> actual = new ArrayList<String>();
        decorated.getSequence(actual);
        assertEquals(expected, actual);
    }
}
