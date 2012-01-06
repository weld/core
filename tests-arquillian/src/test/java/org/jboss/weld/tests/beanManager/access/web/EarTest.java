/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.beanManager.access.web;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import javax.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Verifies that each web application obtains the correct BeanManager from the {@link ServletContext}.
 * 
 * @author Jozef Hartinger
 * 
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class EarTest {

    @ArquillianResource(Servlet1.class)
    private URL url1;

    @ArquillianResource(Servlet2.class)
    private URL url2;

    @Deployment(testable = false)
    public static EnterpriseArchive getDeployment() {
        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "test1.war").addClasses(Foo.class, Bar.class, Servlet1.class, VerifyingListener.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        // war2 is equal to war1 but has the Bar alternative enabled, which overrides Foo
        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "test2.war").addClasses(Foo.class, Bar.class, Servlet2.class, VerifyingListener.class)
                .addAsWebInfResource(new BeansXml().alternatives(Bar.class), "beans.xml");
        return ShrinkWrap.create(EnterpriseArchive.class).addAsModules(war1, war2).addAsManifestResource(EarTest.class.getPackage(), "application.xml", "application.xml");
    }

    @Test
    public void testCorrectBeanManagerAvailable() throws Exception {
        WebClient client = new WebClient();

        TextPage page1 = client.getPage(url1);
        assertEquals("foo,foo", page1.getContent());

        TextPage page2 = client.getPage(url2);
        assertEquals("bar,bar", page2.getContent());
    }
}
