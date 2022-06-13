/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.crosscontext;

import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toContextParam;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toServletAndMapping;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

@RunAsClient
@RunWith(Arquillian.class)
public class CrossContextForwardTest {

    protected static final String FIRST = "first";
    protected static final String SECOND = "second";

    protected static final Asset FORWARDING_WEB_XML = new ByteArrayAsset(extendDefaultWebXml(
            toServletAndMapping("Forwarding Servlet", ForwardingServlet.class, "/forwarding") + toContextParam("WELD_CONTEXT_ID_KEY", FIRST)).getBytes());

    protected static final Asset INCLUDED_WEB_XML = new ByteArrayAsset(extendDefaultWebXml(
            toServletAndMapping("Included Servlet", IncludedServlet.class, "/included") + toContextParam("WELD_CONTEXT_ID_KEY", SECOND)).getBytes());

    @Deployment(name = CrossContextForwardTest.FIRST)
    public static WebArchive createFirstTestArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "app1.war")
                .addAsWebInfResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml").setWebXML(FORWARDING_WEB_XML);
        war.addClass(ForwardingServlet.class);
        return war;
    }

    @Deployment(name = CrossContextForwardTest.SECOND)
    public static WebArchive createSecondTestArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "app2.war")
                .addAsWebInfResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml").setWebXML(INCLUDED_WEB_XML);
        war.addClass(IncludedServlet.class);
        return war;
    }

    @Test
    public void testCrossContextForward(@ArquillianResource @OperateOnDeployment(FIRST) URL firstContext) throws IOException {
        Page page = new WebClient().getPage(firstContext + "forwarding");
        assertEquals(200, page.getWebResponse().getStatusCode());
        assertEquals("<h1>Hello World</h1>", page.getWebResponse().getContentAsString());
    }
}