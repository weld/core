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

package org.jboss.weld.tests.jsf.managedbeans;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Verifies that a non-CDI web module with JSF works fine when a different module of an EAR is CDI-enabled.
 *
 * @see WFLY-3196
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ManagedBeansWithCDITest {

    @Deployment(testable = false)
    public static Archive<?> deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class,
                Utils.getDeploymentNameAsHash(ManagedBeansWithCDITest.class, Utils.ARCHIVE_TYPE.EAR));
        ear.addAsModule(ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebResource(ManagedBeansWithCDITest.class.getPackage(), "index.xhtml", "index.xhtml")
                .addAsWebResource(ManagedBeansWithCDITest.class.getPackage(), "timezones.xhtml", "timezones.xhtml")
                .setWebXML(ManagedBeansWithCDITest.class.getPackage(), "web.xml"));
        ear.addAsLibrary(
                ShrinkWrap.create(JavaArchive.class, "test.jar").addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
        return ear;

    }

    @Test
    @RunAsClient
    public void testManagedBeansWithCDI(@ArquillianResource URL url) throws Exception {
        WebClient client = new WebClient();
        client.getPage(url + "/index.faces");
    }

}