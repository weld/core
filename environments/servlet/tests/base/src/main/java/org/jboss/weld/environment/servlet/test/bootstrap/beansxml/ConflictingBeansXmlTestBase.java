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

package org.jboss.weld.environment.servlet.test.bootstrap.beansxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.junit.Test;

/**
 * If a war has a file named beans.xml in both the WEB-INF directory and in the WEB-INF/classes/META-INF directory, then non-portable behavior results. Weld
 * simply ignores the descriptor from WEB-INF/classes/META-INF.
 *
 * @author Martin Kouba
 */
public class ConflictingBeansXmlTestBase implements Marker {

    public static WebArchive baseTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addClasses(ConflictingBeansXmlTestBase.class, Foo.class, VerifyExtension.class)
                .add(new BeansXml(), "WEB-INF/classes/META-INF/beans.xml").add(new BeansXml().alternatives(Foo.class), "WEB-INF/beans.xml")
                .addAsServiceProvider(Extension.class, VerifyExtension.class);
    }

    @Inject
    Instance<Foo> fooInstance;

    @Inject
    VerifyExtension extension;

    @Test
    public void testConflictingDescriptors() {
        List<Object> events = extension.getEvents();
        // ConflictingBeansXmlTestBase, ConflictingBeansXmlTest, VerifyExtension, Foo, AfterBeanDiscovery
        assertEquals(events.toString(), 5, events.size());
        assertFalse(fooInstance.isUnsatisfied());
        assertFalse(fooInstance.isAmbiguous());
        fooInstance.get().ping();
    }

}
