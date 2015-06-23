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
package org.jboss.weld.environment.servlet.test.context.async.symmetry;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletRequestListener;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Tests that a {@link ServletRequestListener} is called symmetrically with respect to the calling thread.
 * See https://issues.apache.org/bugzilla/show_bug.cgi?id=57314 for details
 *
 * @author Jozef Hartinger
 *
 */
@RunAsClient
@RunWith(Arquillian.class)
public class ListenerSymmetryTest {

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment().addClasses(SimpleServlet.class, SimpleListener.class);
    }

    @Test
    public void testListenerCalledSymmetrically() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        WebClient webClient = new WebClient();
        assertEquals("Async OK", webClient.getPage(contextPath + "async").getWebResponse().getContentAsString().trim());
        assertEquals("OK", webClient.getPage(contextPath + "result").getWebResponse().getContentAsString().trim());
    }
}
