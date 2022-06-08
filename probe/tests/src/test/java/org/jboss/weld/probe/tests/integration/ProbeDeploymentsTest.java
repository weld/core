/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe.tests.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.jboss.weld.probe.Strings.BEAN_DISCOVERY_MODE;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.DEPLOYMENT_PATH;
import static org.jboss.weld.probe.tests.integration.JSONTestUtil.getDeploymentByName;

import java.io.IOException;
import java.net.URL;

import javax.json.JsonObject;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.probe.tests.integration.deployment.beans.almostAlternative.AlmostAlternativeBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ProbeDeploymentsTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-deployments-test-explicit";
    private static final String TEST_IMPLICIT_ARCHIVE_NAME = "probe-deployments-test-implicit";
    private static final String NOT_BEAN_ARCHIVE_NAME = "probe-deployments-test-none";
    private static final String PRIORITY_NON_ALTERNATIVE_BEAN_ARCHIVE_NAME = "probe-deployments-test-priority";
    private static final String EXPLICIT_ARCHIVE = "explicit-archive";
    private static final String IMPLICIT_ARCHIVE = "implicit-archive";
    private static final String NON_BEAN_ARCHIVE = "not-bean-archive";
    private static final String PRIORITY_NON_ALTERNATIVE_BEAN_ARCHIVE = "priority-non-alternative-bean-archive";

    @Deployment(testable = false, name = EXPLICIT_ARCHIVE)
    public static WebArchive deployExplicitArchive() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeDeploymentsTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeDeploymentsTest.class.getPackage(), "beans.xml", "beans.xml");
    }

    @Deployment(testable = false, name = NON_BEAN_ARCHIVE)
    public static WebArchive deployNotBeanArchive() {
        return ShrinkWrap.create(WebArchive.class, NOT_BEAN_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeDeploymentsTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeDeploymentsTest.class.getPackage(), "beans-bdm-none.xml", "beans.xml");
    }

    @Deployment(testable = false, name = IMPLICIT_ARCHIVE)
    public static WebArchive deployImplicitArchive() {
        return ShrinkWrap.create(WebArchive.class, TEST_IMPLICIT_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeDeploymentsTest.class.getPackage(), "web.xml", "web.xml")
                .addPackage(ModelBean.class.getPackage());
    }

    @Deployment(testable = false, name = PRIORITY_NON_ALTERNATIVE_BEAN_ARCHIVE)
    public static WebArchive deployArchiveWithPriorityNonAlternativeBean() {
        return ShrinkWrap.create(WebArchive.class, PRIORITY_NON_ALTERNATIVE_BEAN_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeDeploymentsTest.class.getPackage(), "web.xml", "web.xml")
                .addClass(AlmostAlternativeBean.class);
    }

    @Test
    @OperateOnDeployment(EXPLICIT_ARCHIVE)
    public void testDeploymentEndpointWithExplicitBeanArchive() throws IOException {
        JsonObject testArchive = getDeploymentByName(DEPLOYMENT_PATH, TEST_ARCHIVE_NAME, url);
        assertNotNull("Cannot find test archive in Probe deployments!", testArchive);
        assertEquals("Another bean discovery mode expected!", BeanDiscoveryMode.ANNOTATED.name(), testArchive.getString(BEAN_DISCOVERY_MODE));
    }

    @Test
    @OperateOnDeployment(IMPLICIT_ARCHIVE)
    public void testDeploymentEndpointWithImplicitBeanArchive() throws IOException {
        JsonObject testArchive = getDeploymentByName(DEPLOYMENT_PATH, TEST_IMPLICIT_ARCHIVE_NAME, url);
        assertNotNull("Cannot find test archive in Probe deployments!", testArchive);
        assertNotNull("Cannot find any value for bean discovery mode!", testArchive.get(BEAN_DISCOVERY_MODE));
        assertEquals("Another bean discovery mode expected!", BeanDiscoveryMode.ANNOTATED.name(), testArchive.getString(BEAN_DISCOVERY_MODE));
    }

    @Test
    @OperateOnDeployment(NON_BEAN_ARCHIVE)
    public void testDeploymentEndpointWithNonBeanArchive() throws IOException {
        //expects exception
        try {
            JsonObject testArchive = getDeploymentByName(DEPLOYMENT_PATH, NOT_BEAN_ARCHIVE_NAME, url);
            fail("Deployment archive must not be obtained since Probe shoudln't start!");
        } catch (FailingHttpStatusCodeException e) {
        }
    }

    @Test
    @OperateOnDeployment(PRIORITY_NON_ALTERNATIVE_BEAN_ARCHIVE)
    public void testDeploymentWithPriorityNonAlternativeBean() throws IOException {
        JsonObject testArchive = getDeploymentByName(DEPLOYMENT_PATH, PRIORITY_NON_ALTERNATIVE_BEAN_ARCHIVE_NAME, url);
        // this should not crash, bean with @Priority and no other annotation is correct
        assertNotNull("Cannot find test archive in Probe deployments!", testArchive);
    }
}
