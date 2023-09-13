/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.jsf.flow;

import static org.jboss.weld.tests.util.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * See also WELD-2448. If multiple deployments share a proxy class from static module (JSF impl in this test case) and each
 * deployment uses a different value
 * for {@link ConfigurationKey#RELAXED_CONSTRUCTION} the proxy instance might not be initialized corretly.
 *
 * @author Martin Kouba
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class FlowMultipleWarTest {

    private static final String WAR1 = "war1";

    private static final String WAR2 = "war2";

    @Deployment(name = WAR1, managed = false, testable = false)
    public static WebArchive createFooTestArchive() {
        WebArchive war1 = ShrinkWrap.create(WebArchive.class, WAR1 + ".war")
                .addAsWebResource(FlowMultipleWarTest.class.getPackage(), "index.xhtml",
                        "index.xhtml")
                .addAsWebResource(FlowMultipleWarTest.class.getPackage(), "simple-flow-flow.xml",
                        "simple-flow/simple-flow-flow.xml")
                .addAsWebResource(FlowMultipleWarTest.class.getPackage(), "simple-flow.xhtml",
                        "simple-flow/simple-flow.xhtml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml").addClass(Foo.class)
                .setWebXML(FlowMultipleWarTest.class.getPackage(), "web.xml");
        return war1;
    }

    @Deployment(name = WAR2, managed = false, testable = false)
    public static WebArchive createBarTestArchive() {
        return ShrinkWrap.create(WebArchive.class, WAR2 + ".war")
                .addAsWebResource(FlowMultipleWarTest.class.getPackage(), "index.xhtml",
                        "index.xhtml")
                .addAsWebResource(FlowMultipleWarTest.class.getPackage(), "simple-flow-flow.xml",
                        "simple-flow/simple-flow-flow.xml")
                .addAsWebResource(FlowMultipleWarTest.class.getPackage(), "simple-flow.xhtml",
                        "simple-flow/simple-flow.xhtml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml").addClass(Bar.class)
                .setWebXML(FlowMultipleWarTest.class.getPackage(), "web.xml").addAsResource(
                        PropertiesBuilder.newBuilder()
                                .set(ConfigurationKey.RELAXED_CONSTRUCTION.get(), "true").build(),
                        "weld.properties");
    }

    @ArquillianResource
    Deployer deployer;

    @Before
    public void deploy() {
        deployer.deploy(WAR1);
        deployer.deploy(WAR2);
    }

    @After
    public void undeploy() {
        deployer.undeploy(WAR1);
        deployer.undeploy(WAR2);
    }

    @Test
    @RunAsClient
    public void testFlows(@ArquillianResource @OperateOnDeployment(WAR1) URL war1Context,
            @ArquillianResource @OperateOnDeployment(WAR2) URL war2Context)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        WebClient client = new WebClient();
        testFlow(client, war1Context);
        testFlow(client, war2Context);
    }

    private void testFlow(WebClient client, URL warContext) throws IOException {
        HtmlPage page = client.getPage(warContext + "/index.faces");
        HtmlSubmitInput startFlowButton = (HtmlSubmitInput) page
                .getElementById("start-simple-flow");
        page = startFlowButton.click();
        assertTrue(page.getWebResponse().getContentAsString().contains("Flow 1"));
    }

}
