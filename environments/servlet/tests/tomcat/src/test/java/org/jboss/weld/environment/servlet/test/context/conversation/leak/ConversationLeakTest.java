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
package org.jboss.weld.environment.servlet.test.context.conversation.leak;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.TomcatDeployments.CONTEXT_XML;

import java.net.URL;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * This test verifies whether the conversation context is susceptible to information disclosure when processing a request after
 * a different request executing in the same thread was not cleaned up properly.
 *
 * This is quite difficult to reproduce thus we use a bug in Tomcat to actually suppress conversation context cleanup and only deactivate
 * the context ourselves (the context is not dissociated).
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class ConversationLeakTest {

    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive deployment() {
        return baseDeployment().addClasses(SimpleServlet.class, ConversationScopedBean.class).addAsWebInfResource(CONTEXT_XML, "META-INF/context.xml");
    }

    @Test
    public void test() throws Exception {
        WebClient webClient = new WebClient();
        webClient.setThrowExceptionOnFailingStatusCode(false);
        final String path = getPath("getAndSet");
        for (int i = 0; i < 100; i++) {
            String result = webClient.getPage(path).getWebResponse().getContentAsString().trim();
            Assert.assertEquals("Information disclosure detected after " + (i + 1) + " requests", "foo", result);
        }
    }

    private String getPath(String test) {
        return contextPath + "/servlet?action=" + test;
    }
}
