/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.enterprise;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URL;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class ExtensionInMultipleWarsTest {

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        JavaArchive lib = ShrinkWrap.create(JavaArchive.class).addClasses(TestExtension.class, TestBean.class)
                .addAsServiceProvider(Extension.class,
                        TestExtension.class);
        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "test1.war").addClass(TestServlet1.class).addAsLibraries(lib)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "test2.war").addClass(TestServlet2.class).addAsLibraries(lib)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(ExtensionInMultipleWarsTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(war1,
                        war2);
    }

    @ArquillianResource(TestServlet1.class)
    private URL contextPath1;

    @ArquillianResource(TestServlet2.class)
    private URL contextPath2;

    @Test
    public void testIsolation() throws FailingHttpStatusCodeException, IOException {
        WebClient client = new WebClient();
        String result1 = client.getPage(contextPath1 + "servlet1").getWebResponse().getContentAsString();
        String result2 = client.getPage(contextPath2 + "servlet2").getWebResponse().getContentAsString();
        assertFalse(result1.equals(result2));
    }
}
