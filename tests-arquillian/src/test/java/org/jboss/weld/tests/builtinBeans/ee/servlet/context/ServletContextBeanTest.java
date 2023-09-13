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
package org.jboss.weld.tests.builtinBeans.ee.servlet.context;

import java.net.URL;

import jakarta.servlet.ServletContext;

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
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 *
 * Tests the built-in bean for {@link ServletContext}.
 *
 * @author Jozef Hartinger
 * @see WELD-1621
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ServletContextBeanTest {

    @ArquillianResource(FooServlet.class)
    private URL fooUrl;

    @ArquillianResource(BarServlet.class)
    private URL barUrl;

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "foo.war").addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(FooServlet.class);
        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "bar.war").addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(BarServlet.class);
        JavaArchive library = ShrinkWrap.create(JavaArchive.class).addClass(SharedBean.class);
        return ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(ServletContextBeanTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(war1, war2).addAsLibrary(library);
    }

    @Test
    public void testFirstArchive() throws Exception {
        WebClient client = new WebClient();
        TextPage page = client.getPage(fooUrl);
        Assert.assertEquals("/foo;/foo", page.getContent());
    }

    @Test
    public void testSecondArchive() throws Exception {
        WebClient client = new WebClient();
        TextPage page = client.getPage(barUrl);
        Assert.assertEquals("/bar;/bar", page.getContent());
    }
}
