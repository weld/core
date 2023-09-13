/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
public class ServletInjectionTest {

    public static final Asset WEB_XML = new ByteArrayAsset(
            extendDefaultWebXml("<servlet><servlet-name>Rat Servlet</servlet-name><servlet-class>" + RatServlet.class.getName()
                    + "</servlet-class></servlet> <servlet-mapping><servlet-name>Rat Servlet</servlet-name><url-pattern>/rat</url-pattern></servlet-mapping>")
                    .getBytes());

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment(WEB_XML).addClasses(RatServlet.class, Sewer.class);
    }

    @Test
    public void testServletInjection(@ArquillianResource URL baseURL) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(new URL(baseURL, "rat").toExternalForm());
        assertEquals(HttpServletResponse.SC_OK, client.execute(request).getStatusLine().getStatusCode());
    }

}
