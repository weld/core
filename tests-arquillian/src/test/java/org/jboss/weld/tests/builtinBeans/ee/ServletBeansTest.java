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
package org.jboss.weld.tests.builtinBeans.ee;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class ServletBeansTest {

    @ArquillianResource(Servlet.class)
    private URL url;
    
    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, "servletBuiltinBeanTest.war").addClasses(Servlet.class, ServletBuiltinBeanInjectingBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testHttpServletRequest() throws Exception {
        WebClient client = new WebClient();
        client.getPage(url + "/request?foo=bar");
    }

    @Test
    public void testHttpSession() throws Exception {
        WebClient client = new WebClient();
        client.getPage(url + "/session");
    }
    
    @Test
    public void testServletContext() throws Exception {
        WebClient client = new WebClient();
        client.getPage(url + "/context");
    }
}
