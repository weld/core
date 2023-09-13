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

package org.jboss.weld.tests.xml.namespaces;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.xml.namespaces.excluded.FooExcluded;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that parsing beans.xml with weld namespace correctly searches for end tags.
 *
 * See WELD-2591
 */
@RunWith(Arquillian.class)
public class BeansXmlTest {

    @Inject
    private Instance<Object> instance;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackage(BeansXmlTest.class.getPackage())
                .addPackage(FooExcluded.class.getPackage())
                .addAsManifestResource(
                        new StringAsset("<beans\n" +
                                "        xmlns=\"http://java.sun.com/xml/ns/javaee\" \n" +
                                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                                "        xmlns:weld=\"http://jboss.org/schema/weld/beans\">\n" +
                                "    <weld:scan>\n" +
                                "    <weld:exclude name=\"org.jboss.weld.tests.xml.namespaces.excluded.FooExcluded\"/>\n" +
                                "    </weld:scan>\n" +
                                "    <alternatives>\n" +
                                "        <class>\n" +
                                "            org.jboss.weld.tests.xml.namespaces.SomeAlternative\n" +
                                "        </class>\n" +
                                "    </alternatives>\n" +
                                "</beans>"),
                        "beans.xml");
    }

    @Test
    public void test() {
        // verify scan exclusion works
        Assert.assertFalse(instance.select(FooExcluded.class).isResolvable());
        // verify alternative is enabled
        Assert.assertTrue(instance.select(SomeAlternative.class).isResolvable());
    }

}
